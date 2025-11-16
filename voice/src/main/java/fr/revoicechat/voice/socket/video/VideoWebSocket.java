package fr.revoicechat.voice.socket.video;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.model.AuthenticatedUser;
import fr.revoicechat.voice.socket.CompletionStageService;
import fr.revoicechat.voice.socket.WebSocketAuthConfigurator;
import fr.revoicechat.voice.socket.video.Viewer.VideoRisks;
import fr.revoicechat.voice.utils.IgnoreExceptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/video/{userId}/{streamName}", configurator = WebSocketAuthConfigurator.class)
@ApplicationScoped
public class VideoWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(VideoWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Map<Streamer, Set<Viewer>> streams = new ConcurrentHashMap<>();

  private final ManagedExecutor executor;
  private final UserHolder userHolder;
  private final CompletionStageService completionStageService;

  public VideoWebSocket(ManagedExecutor executor,
                        UserHolder userHolder,
                        CompletionStageService completionStageService) {
    this.executor = executor;
    this.userHolder = userHolder;
    this.completionStageService = completionStageService;
  }

  @OnOpen
  @SuppressWarnings("unused") // call by websocket listener
  public void onOpen(Session session, @PathParam("userId") String userIdAsString, @PathParam("streamName") String streamName) {
    String token = token(session);
    if (token == null) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Missing token");
      return;
    }
    UUID userId;
    try {
      userId = userHolder.peekId(token);
    } catch (Exception e) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token: " + e.getMessage());
      return;
    }
    completionStageService.enqueue(userId, session, () -> CompletableFuture.runAsync(() -> handleOpen(session, token, userIdAsString, streamName), executor));
  }

  @Transactional
  void handleOpen(Session session, String token, String userIdAsString, final String streamName) {
    UUID streamedUserId = UUID.fromString(userIdAsString);
    var user = userHolder.get(token);
    if (user == null) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token");
      return;
    }
    if (streamedUserId.equals(user.getId())) {
      openStream(session, streamedUserId, streamName);
    } else {
      subscribeStream(session, user, streamedUserId, streamName);
    }
  }

  private void openStream(final Session session, final UUID streamedUserId, final String streamName) {
    var stream = get(streamedUserId, streamName);
    closeOldSession(stream);
    var streamer = new Streamer(streamedUserId, streamName, session);
    streams.put(streamer, ConcurrentHashMap.newKeySet());
  }

  private void subscribeStream(final Session session, final AuthenticatedUser user, final UUID streamedUserId, final String streamName) {
    var stream = get(streamedUserId, streamName);
    if (stream == null) {
      closeSession(session, CloseCodes.CANNOT_ACCEPT, "No stream found");
      return;
    }
    stream.getValue().add(new Viewer(user.getId(), new VideoRisks(true, true, true), session));
  }

  private String token(Session session) {
    return Optional.ofNullable(session.getUserProperties())
                   .map(props -> props.get("auth-token"))
                   .map(String.class::cast)
                   .orElseGet(() -> tokenFromQueryParam(session));
  }

  private static String tokenFromQueryParam(final Session session) {
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.getOrDefault("token", List.of());
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.getFirst();
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
    LOG.debug("Text message from {}: {}", sender.getId(), message);
    onMessage(sender, session -> session.getAsyncRemote().sendText(message));
  }

  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(byte[] message, Session sender) {
    LOG.debug("Client {} send : {}", sender.getId(), message);
    onMessage(sender, session -> session.getAsyncRemote().sendBinary(ByteBuffer.wrap(message)));
  }

  private void onMessage(Session sender, Consumer<Session> action) {
    var current = get(sender);
    if (current == null) {
      return;
    }
    current.getValue()
           .stream()
           .map(Viewer::session)
           .filter(session -> !session.getId().equals(sender.getId()))
           .filter(Session::isOpen)
           .forEach(action);
  }

  @OnClose
  @SuppressWarnings("unused") // call by websocket listener
  public void onClose(Session session) {
    var stream = get(session);
    if (stream != null) {
      completionStageService.enqueue(
          stream.getKey().user(),
          session,
          () -> CompletableFuture.runAsync(() -> closeOldSession(stream), executor)
      );
    } else {
      var closingViewer = getClosingViewer(session);
      if (closingViewer != null) {
        completionStageService.enqueue(
            closingViewer.viewer().user(),
            session,
            () -> CompletableFuture.runAsync(() -> handleCloseSession(closingViewer.viewer()), executor)
        );
        closingViewer.viewers().remove(closingViewer.viewer());
      }
    }
  }

  private ClosingViewer getClosingViewer(final Session session) {
    for (final Set<Viewer> value : streams.values()) {
      for (final Viewer viewer : value) {
        if (viewer.session().equals(session)) {
          return new ClosingViewer(value, viewer);
        }
      }
    }
    return null;
  }

  private record ClosingViewer(Set<Viewer> viewers, Viewer viewer) {}

  @Transactional
  public void handleCloseSession(final VideoUser user) {
    LOG.info("Client disconnected: {}", user.session().getId());
    closeSession(user.session(), CloseCodes.NORMAL_CLOSURE, "Client disconnected");
  }

  @OnError
  @SuppressWarnings("unused") // call by websocket listener
  public void onError(Session session, Throwable throwable) {
    LOG.error("Error on session {}: {}", session.getId(), throwable.getMessage());
  }

  private void closeSession(Session session, CloseCodes code, String reason) {
    IgnoreExceptions.run(() -> session.close(new CloseReason(code, reason)));
  }

  private Entry<Streamer, Set<Viewer>> get(Session session) {
    return streams.entrySet().stream()
                  .filter(e -> e.getKey().session().equals(session))
                  .findFirst()
                  .orElse(null);
  }

  private Entry<Streamer, Set<Viewer>> get(UUID streamedUserId, String streamName) {
    var streamer = new Streamer(streamedUserId, streamName, null);
    return streams.entrySet().stream()
                  .filter(e -> e.getKey().equals(streamer))
                  .findFirst()
                  .orElse(null);
  }
}
