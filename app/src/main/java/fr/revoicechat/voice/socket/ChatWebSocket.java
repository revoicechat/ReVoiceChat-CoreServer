package fr.revoicechat.voice.socket;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.voice.notification.VoiceJoiningNotification;
import fr.revoicechat.voice.notification.VoiceLeavingNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.WebApplicationException;

@ServerEndpoint("/voice")
@ApplicationScoped
public class ChatWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<UserSession> sessions = ConcurrentHashMap.newKeySet();

  private final ManagedExecutor executor;
  private final UserHolder userHolder;

  public ChatWebSocket(final ManagedExecutor executor, final UserHolder userHolder) {
    this.executor = executor;
    this.userHolder = userHolder;
  }

  @OnOpen
  public void onOpen(Session session) {
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.getOrDefault("token", List.of());
    if (tokens.isEmpty()) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Missing token");
      return;
    }
    String token = tokens.getFirst();
    try {
      executor.submit(() -> {
        var user = userHolder.get(token);
        closeOldSession(user);
        LOG.info("WebSocket connected as user {}", user.getId());
        sessions.add(new UserSession(user.getId(), session));
        Notification.of(new VoiceJoiningNotification(user.getId())).sendTo(Stream.of(user));
      });
    } catch (WebApplicationException e) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token: " + e.getMessage());
    }
  }

  private void closeOldSession(final User user) {
    var existingSession = getExistingSession(user.getId());
    if (existingSession != null) {
      onClose(existingSession.session);
    }
  }

  @OnMessage
  public void onTextMessage(String message, Session sender) {
    LOG.debug("Text message from {}: {}", sender.getId(), message);
    // Relay text to all other clients
    for (UserSession userSession : sessions) {
      var session = userSession.session;
      if (!session.equals(sender) && session.isOpen()) {
        session.getAsyncRemote().sendText(message);
      }
    }
  }

  @OnMessage
  public void onMessage(byte[] message, Session sender) {
    // Relay message to all other connected clients
    LOG.debug("Client {} send : {}", sender.getId(), message);
    for (UserSession userSession : sessions) {
      var session = userSession.session;
      if (!session.getId().equals(sender.getId()) && session.isOpen()) {
        session.getAsyncRemote().sendBinary(ByteBuffer.wrap(message));
      }
    }
  }

  @OnClose
  public void onClose(Session session) {
    LOG.info("Client disconnected: {}", session.getId());
    sessions.stream().filter(userSession -> userSession.session.equals(session))
            .findFirst()
            .ifPresent(userSession -> {
              Notification.of(new VoiceLeavingNotification(userSession.user)).sendTo(Stream.of(() -> userSession.user));
              closeSession(userSession.session, CloseCodes.NORMAL_CLOSURE, "Client disconnected");
              sessions.remove(userSession);
            });
  }

  private UserSession getExistingSession(UUID user) {
    return sessions.stream().filter(userSession -> userSession.user.equals(user))
            .findFirst()
            .orElse(null);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    LOG.error("Error on session {}: {}", session.getId(), throwable.getMessage());
  }

  private record UserSession(UUID user, Session session) {}

  private void closeSession(Session session, CloseCodes code, String reason) {
    try {
      session.close(new CloseReason(code, reason));
    } catch (Exception ignored) {
      // ignored the following error
    }
  }
}
