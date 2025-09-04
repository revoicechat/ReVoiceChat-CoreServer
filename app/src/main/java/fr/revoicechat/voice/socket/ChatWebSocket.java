package fr.revoicechat.voice.socket;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.service.room.ConnectedUserRetriever;
import fr.revoicechat.core.utils.IgnoreExceptions;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.representation.UserNotificationRepresentation;
import fr.revoicechat.voice.notification.VoiceJoiningNotification;
import fr.revoicechat.voice.notification.VoiceLeavingNotification;

@ServerEndpoint("/voice/{roomId}")
@ApplicationScoped
public class ChatWebSocket implements ConnectedUserRetriever {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<UserSession> sessions = ConcurrentHashMap.newKeySet();

  private final ManagedExecutor executor;
  private final EntityManager entityManager;
  private final UserHolder userHolder;

  public ChatWebSocket(ManagedExecutor executor, EntityManager entityManager, UserHolder userHolder) {
    this.executor = executor;
    this.entityManager = entityManager;
    this.userHolder = userHolder;
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
    executor.submit(() -> {
      try {
        var user = userHolder.get(token);
        closeOldSession(user);
        var room = entityManager.find(Room.class, roomId);
        if (room == null || !room.getType().equals(RoomType.VOICE)) {
          closeSession(session, CloseCodes.CANNOT_ACCEPT, "selected room cannot accept websocket chat type");
          return;
        }
        LOG.info("WebSocket connected as user {}", user.getId());
        sessions.add(new UserSession(user.getId(), roomId, session));
        Notification.of(new VoiceJoiningNotification(new UserNotificationRepresentation(user.getId(), user.getDisplayName()), roomId)).sendTo(Stream.of(user));
      } catch (Exception e) {
        closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token: " + e.getMessage());
      }
    });
  }

  private String token(Session session) {
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.getOrDefault("token", List.of());
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.getFirst();
  }

  private void closeOldSession(final User user) {
    var existingSession = getExistingSession(user.getId());
    if (existingSession != null) {
      onClose(existingSession.session);
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
  public void onClose(Session session) {
    LOG.info("Client disconnected: {}", session.getId());
    sessions.stream().filter(userSession -> userSession.session.equals(session))
            .findFirst()
            .ifPresent(userSession -> {
              Notification.of(new VoiceLeavingNotification(userSession.user, userSession.room)).sendTo(Stream.of(() -> userSession.user));
              closeSession(userSession.session, CloseCodes.NORMAL_CLOSURE, "Client disconnected");
              sessions.remove(userSession);
            });
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

  private record UserSession(UUID user, UUID room, Session session) {}

  private void closeSession(Session session, CloseCodes code, String reason) {
    IgnoreExceptions.run(() -> session.close(new CloseReason(code, reason)));
  }
}
