package fr.revoicechat.live.stream.socket;

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

import fr.revoicechat.live.common.service.DiscussionRiskService;
import fr.revoicechat.live.common.socket.WebSocketAuthConfigurator;
import fr.revoicechat.live.common.socket.WebSocketService;
import fr.revoicechat.live.stream.notification.StreamJoin;
import fr.revoicechat.live.stream.notification.StreamLeave;
import fr.revoicechat.live.stream.notification.StreamStart;
import fr.revoicechat.live.stream.notification.StreamStop;
import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import fr.revoicechat.live.voice.service.VoiceRoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.security.UserHolder;

@ServerEndpoint(value = "/stream/{userId}/{name}", configurator = WebSocketAuthConfigurator.class)
@ApplicationScoped
public class StreamWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(StreamWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<StreamSession> streams = ConcurrentHashMap.newKeySet();

  private final WebSocketService webSocketService;
  private final UserHolder userHolder;
  private final ConnectedUserRetriever connectedUserRetriever;
  private final DiscussionRiskService discussionRiskService;
  private final VoiceRoomUserFinder roomUserFinder;

  public StreamWebSocket(final WebSocketService webSocketService,
                         final UserHolder userHolder,
                         final ConnectedUserRetriever connectedUserRetriever,
                         final DiscussionRiskService discussionRiskService,
                         final VoiceRoomUserFinder roomUserFinder) {
    this.webSocketService = webSocketService;
    this.userHolder = userHolder;
    this.connectedUserRetriever = connectedUserRetriever;
    this.discussionRiskService = discussionRiskService;
    this.roomUserFinder = roomUserFinder;
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
      startStream(new Streamer(streamedUserId, streamName, risks, session), room);
    } else {
      joinStream(new Viewer(user.getId(), risks, session), streamedUserId, streamName, room);
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
    if (current == null || !current.streamer().risks().send()) {
      return Stream.empty();
    }
    return current.viewers()
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
      var room = connectedUserRetriever.getRoomForUser(stream.streamer().user());
      webSocketService.closeSession(stream.streamer().user(), session, () -> stopStream(stream, room));
    } else {
      leaveStream(session);
    }
  }


  private void startStream(Streamer streamer, UUID roomId) {
    if (!streamer.risks().send()) {
      webSocketService.closeSession(streamer.session(), CloseCodes.CANNOT_ACCEPT, "User in not allowed to stream in this room");
      return;
    }
    var stream = get(streamer.user(), streamer.streamName());
    stopStream(stream, roomId);
    streams.add(new StreamSession(streamer));
    Notification.of(new StreamStart(streamer.user(), streamer.streamName())).sendTo(roomUserFinder.find(roomId));
  }

  private void joinStream(Viewer viewer, UUID streamedUserId, String streamName, UUID roomId) {
    if (!viewer.risks().receive()) {
      webSocketService.closeSession(viewer.session(), CloseCodes.CANNOT_ACCEPT, "User in not allowed to watch a stream in this room");
      return;
    }
    var stream = get(streamedUserId, streamName);
    if (stream == null) {
      webSocketService.closeSession(viewer.session(), CloseCodes.CANNOT_ACCEPT, "No stream found");
      return;
    }
    stream.viewers().add(viewer);
    Notification.of(new StreamJoin(streamedUserId, streamName, viewer.user())).sendTo(roomUserFinder.find(roomId));
  }

  private void leaveStream(final Session session) {
    var closingViewer = getClosingViewer(session);
    if (closingViewer != null) {
      webSocketService.closeSession(closingViewer.viewer().user(), session, () -> handleCloseSession(closingViewer.viewer()));
      closingViewer.stream().remove(closingViewer.viewer());
      var streamer = closingViewer.stream().streamer();
      var room = connectedUserRetriever.getRoomForUser(streamer.user());
      Notification.of(new StreamLeave(streamer.user(), streamer.streamName(), closingViewer.viewer().user())).sendTo(roomUserFinder.find(room));
    }
  }

  private void stopStream(StreamSession stream, UUID roomId) {
    if (stream != null) {
      stream.viewers().forEach(this::handleCloseSession);
      handleCloseSession(stream.streamer());
      streams.remove(stream);
      Notification.of(new StreamStop(stream.streamer().user(), stream.streamer().streamName())).sendTo(roomUserFinder.find(roomId));
    }
  }

  private ClosingViewer getClosingViewer(final Session session) {
    for (final StreamSession streamSession : streams) {
      for (final Viewer viewer : streamSession.viewers()) {
        if (viewer.is(session)) {
          return new ClosingViewer(streamSession, viewer);
        }
      }
    }
    return null;
  }

  private record ClosingViewer(StreamSession stream, Viewer viewer) {}

  @Transactional
  void handleCloseSession(final StreamAgent user) {
    LOG.info("Client disconnected: {}", user);
    webSocketService.closeSession(user.session(), CloseCodes.NORMAL_CLOSURE, "Client disconnected");
  }

  @OnError
  @SuppressWarnings("unused") // call by websocket listener
  public void onError(Session session, Throwable throwable) {
    webSocketService.onError(session, throwable);
  }

  private StreamSession get(Session session) {
    return streams.stream()
                  .filter(e -> e.streamer().is(session))
                  .findFirst()
                  .orElse(null);
  }

  private StreamSession get(UUID streamedUserId, String streamName) {
    return streams.stream()
                  .filter(e -> e.streamer().is(streamedUserId, streamName))
                  .findFirst()
                  .orElse(null);
  }
}
