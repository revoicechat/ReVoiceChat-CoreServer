package fr.revoicechat.voice.socket;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.representation.UserNotificationRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.model.AuthenticatedUser;
import fr.revoicechat.voice.notification.VoiceJoiningNotification;
import fr.revoicechat.voice.notification.VoiceLeavingNotification;
import fr.revoicechat.voice.service.room.VoiceRoomPredicate;
import fr.revoicechat.voice.service.user.ConnectedUserRetriever;
import fr.revoicechat.voice.service.user.VoiceRoomUserFinder;
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

@ServerEndpoint("/voice/{roomId}")
@ApplicationScoped
public class ChatWebSocket implements ConnectedUserRetriever {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<UserSession> sessions = ConcurrentHashMap.newKeySet();
  // One queue per user (serialized execution)
  private final ConcurrentHashMap<UUID, CompletableFuture<Void>> userQueues = new ConcurrentHashMap<>();

  private final ManagedExecutor executor;
  private final UserHolder userHolder;
  private final VoiceRoomUserFinder roomUserFinder;
  private final VoiceRoomPredicate voiceRoomPredicate;

  public ChatWebSocket(ManagedExecutor executor,
                       UserHolder userHolder,
                       VoiceRoomUserFinder roomUserFinder,
                       VoiceRoomPredicate voiceRoomPredicate) {
    this.executor = executor;
    this.userHolder = userHolder;
    this.roomUserFinder = roomUserFinder;
    this.voiceRoomPredicate = voiceRoomPredicate;
  }

  @OnOpen
  @SuppressWarnings("unused") // call by websocket listener
  public void onOpen(Session session, @PathParam("roomId") String roomIdAsString) {
    UUID roomId = UUID.fromString(roomIdAsString);
    String token = token(session);
    if (token == null) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Missing token");
      return;
    }
    UUID userId;
    try {
      userId = userHolder.peekId(token); // lightweight parse, no DB hit
    } catch (Exception e) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token: " + e.getMessage());
      return;
    }
    // enqueue the actual heavy work
    enqueue(userId, session, () -> CompletableFuture.runAsync(() -> handleOpen(session, token, roomId), executor));
  }

  /**
   * The actual heavy open logic (DB, session, notification).
   * Runs inside the ManagedExecutor with @Transactional context.
   */
  @Transactional
  void handleOpen(Session session, String token, UUID roomId) {
    var user = userHolder.get(token);
    closeOldSession(user);
    if (!voiceRoomPredicate.isVoiceRoom(roomId)) {
      closeSession(session, CloseCodes.CANNOT_ACCEPT, "Selected room cannot accept websocket chat type");
      return;
    }
    LOG.info("WebSocket connected as user {}", user.getId());
    sessions.add(new UserSession(user.getId(), roomId, session));
    var data = new VoiceJoiningNotification(new UserNotificationRepresentation(user.getId(), user.getDisplayName()), roomId);
    Notification.of(data).sendTo(roomUserFinder.find(roomId));
  }

  private String token(Session session) {
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.getOrDefault("token", List.of());
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.getFirst();
  }

  private void closeOldSession(final AuthenticatedUser user) {
    var existingSession = getExistingSession(user.getId());
    if (existingSession != null) {
      handleCloseSession(existingSession);
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
    var current = getExistingSession(sender);
    sessions.stream()
            .filter(userSession -> userSession.room.equals(current.room))
            .map(userSession -> userSession.session)
            .filter(session -> !session.getId().equals(sender.getId()))
            .filter(Session::isOpen)
            .forEach(action);
  }

  @OnClose
  @SuppressWarnings("unused") // call by websocket listener
  public void onClose(Session session) {
    sessions.stream().filter(userSession -> userSession.session.equals(session))
            .findFirst()
            .ifPresent(userSession -> enqueue(userSession.user, session, () -> CompletableFuture.runAsync(() -> handleCloseSession(userSession), executor)));
  }

  @Transactional
  public void handleCloseSession(final UserSession userSession) {
    LOG.info("Client disconnected: {}", userSession.session.getId());
    Notification.of(new VoiceLeavingNotification(userSession.user, userSession.room)).sendTo(roomUserFinder.find(userSession.room));
    closeSession(userSession.session, CloseCodes.NORMAL_CLOSURE, "Client disconnected");
    sessions.remove(userSession);
  }

  private UserSession getExistingSession(UUID user) {
    return sessions.stream().filter(userSession -> userSession.user.equals(user))
                   .findFirst()
                   .orElse(null);
  }

  private UserSession getExistingSession(Session session) {
    return sessions.stream().filter(userSession -> userSession.session.getId().equals(session.getId()))
                   .findFirst()
                   .orElse(null);
  }

  @OnError
  @SuppressWarnings("unused") // call by websocket listener
  public void onError(Session session, Throwable throwable) {
    LOG.error("Error on session {}: {}", session.getId(), throwable.getMessage());
  }

  @Override
  public Stream<UUID> getConnectedUsers(UUID room) {
    return sessions.stream()
                   .filter(userSession -> userSession.room.equals(room))
                   .map(UserSession::user);
  }

  public record UserSession(UUID user, UUID room, Session session) {}

  private void closeSession(Session session, CloseCodes code, String reason) {
    IgnoreExceptions.run(() -> session.close(new CloseReason(code, reason)));
  }

  /**
   * Utility: enqueue a task for a specific user, so tasks run sequentially.
   */
  private void enqueue(UUID userId, Session session, Supplier<CompletionStage<Void>> task) {
    userQueues.compute(userId, (id, prev) -> {
      CompletableFuture<Void> start = (prev == null ? CompletableFuture.completedFuture(null) : prev);
      return start
          .thenComposeAsync(v -> task.get(), executor)
          .exceptionally(t -> {
            LOG.error("Error handling user {}", userId, t);
            closeSession(session, CloseCodes.PROTOCOL_ERROR, "Error handling user " + userId);
            return null;
          });
    });
  }
}
