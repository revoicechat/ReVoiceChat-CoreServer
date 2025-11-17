package fr.revoicechat.live.stream.socket;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.model.AuthenticatedUser;
import fr.revoicechat.live.risk.LiveDiscussionRisks;
import fr.revoicechat.live.common.service.DiscussionRiskService;
import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import fr.revoicechat.live.common.socket.WebSocketAuthConfigurator;
import fr.revoicechat.live.common.socket.WebSocketService;

@ServerEndpoint(value = "/stream/{userId}/{name}", configurator = WebSocketAuthConfigurator.class)
@ApplicationScoped
public class StreamWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(StreamWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Map<Streamer, Set<Viewer>> streams = new ConcurrentHashMap<>();

  private final WebSocketService webSocketService;
  private final UserHolder userHolder;
  private final ConnectedUserRetriever connectedUserRetriever;
  private final DiscussionRiskService discussionRiskService;

  public StreamWebSocket(final WebSocketService webSocketService,
                         final UserHolder userHolder,
                         final ConnectedUserRetriever connectedUserRetriever,
                         final DiscussionRiskService discussionRiskService) {
    this.webSocketService = webSocketService;
    this.userHolder = userHolder;
    this.connectedUserRetriever = connectedUserRetriever;
    this.discussionRiskService = discussionRiskService;
  }

  @OnOpen
  @SuppressWarnings("unused") // call by websocket listener
  public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("name") String name) {
    webSocketService.onOpen(session, token -> handleOpen(session, token, userId, name));
  }

  @Transactional
  void handleOpen(Session session, String token, String userIdAsString, final String streamName) {
    UUID streamedUserId = UUID.fromString(userIdAsString);
    var user = userHolder.get(token);
    if (user == null) {
      webSocketService.closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token");
      return;
    }
    var room = connectedUserRetriever.getRoomForUser(user.getId());
    if (room == null) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "User is not connected to a room");
      return;
    }
    var risks = discussionRiskService.getStreamRisks(room, user.getId());
    if (streamedUserId.equals(user.getId())) {
      openStream(session, streamedUserId, streamName, risks);
    } else {
      subscribeStream(session, user, streamedUserId, streamName, risks);
    }
  }

  private void openStream(final Session session, final UUID streamedUserId, final String streamName, final LiveDiscussionRisks risks) {
    if (!risks.send()) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "User in not allowed to stream in this room");
      return;
    }
    var stream = get(streamedUserId, streamName);
    closeOldSession(stream);
    var streamer = new Streamer(streamedUserId, streamName, risks, session);
    streams.put(streamer, ConcurrentHashMap.newKeySet());
  }

  private void subscribeStream(final Session session, final AuthenticatedUser user, final UUID streamedUserId, final String streamName, final LiveDiscussionRisks risks) {
    if (!risks.receive()) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "User in not allowed to watch a stream in this room");
      return;
    }
    var stream = get(streamedUserId, streamName);
    if (stream == null) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "No stream found");
      return;
    }
    stream.getValue().add(new Viewer(user.getId(), risks, session));
  }

  private void closeOldSession(Entry<Streamer, Set<Viewer>> stream) {
    if (stream != null) {
      stream.getValue().forEach(this::handleCloseSession);
      handleCloseSession(stream.getKey());
      streams.remove(stream.getKey());
    }
  }

  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(String message, Session sender) {
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(byte[] message, Session sender) {
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  private Stream<Session> getReceiver(Session sender) {
    var current = get(sender);
    if (current == null || !current.getKey().risks().send()) {
      return Stream.empty();
    }
    return current.getValue()
                  .stream()
                  .filter(viewer -> viewer.risks().receive())
                  .map(Viewer::session)
                  .filter(session -> !session.getId().equals(sender.getId()));
  }

  @OnClose
  @SuppressWarnings("unused") // call by websocket listener
  public void onClose(Session session) {
    var stream = get(session);
    if (stream != null) {
      webSocketService.closeSession(stream.getKey().user(), session, () -> closeOldSession(stream));
    } else {
      var closingViewer = getClosingViewer(session);
      if (closingViewer != null) {
        webSocketService.closeSession(closingViewer.viewer().user(), session, () -> handleCloseSession(closingViewer.viewer()));
        closingViewer.viewers().remove(closingViewer.viewer());
      }
    }
  }

  private ClosingViewer getClosingViewer(final Session session) {
    for (final Set<Viewer> value : streams.values()) {
      for (final Viewer viewer : value) {
        if (viewer.is(session)) {
          return new ClosingViewer(value, viewer);
        }
      }
    }
    return null;
  }

  private record ClosingViewer(Set<Viewer> viewers, Viewer viewer) {}

  @Transactional
  public void handleCloseSession(final StreamSession user) {
    LOG.info("Client disconnected: {}", user.sessionId());
    webSocketService.closeSession(user.session(), CloseCodes.NORMAL_CLOSURE, "Client disconnected");
  }

  @OnError
  @SuppressWarnings("unused") // call by websocket listener
  public void onError(Session session, Throwable throwable) {
    webSocketService.onError(session, throwable);
  }

  private Entry<Streamer, Set<Viewer>> get(Session session) {
    return streams.entrySet().stream()
                  .filter(e -> e.getKey().is(session))
                  .findFirst()
                  .orElse(null);
  }

  private Entry<Streamer, Set<Viewer>> get(UUID streamedUserId, String streamName) {
    return streams.entrySet().stream()
                  .filter(e -> e.getKey().is(streamedUserId, streamName))
                  .findFirst()
                  .orElse(null);
  }
}
