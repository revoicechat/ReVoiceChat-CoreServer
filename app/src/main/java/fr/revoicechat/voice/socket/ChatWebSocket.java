package fr.revoicechat.voice.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.voice.notification.VoiceJoiningNotification;
import fr.revoicechat.voice.notification.VoiceLeavingNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.WebApplicationException;

@ServerEndpoint("/vocal")
@ApplicationScoped
public class ChatWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<UserSession> sessions = ConcurrentHashMap.newKeySet();

  private final UserHolder userHolder;

  public ChatWebSocket(final UserHolder userHolder) {
    this.userHolder = userHolder;
  }

  @OnOpen
  public void onOpen(Session session, EndpointConfig config) {
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.get("token");
    if (tokens == null || tokens.isEmpty()) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Missing token");
      return;
    }
    String token = tokens.getFirst();
    try {
      var user = userHolder.get(token);
      LOG.info("WebSocket connected as user {}", user.getId());
      sessions.add(new UserSession(user.getId(), session));
      Notification.of(new VoiceJoiningNotification(user.getId())).sendTo(Stream.of(user));
    } catch (WebApplicationException e) {
      closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token: " + e.getMessage());
    }
  }

  @OnMessage
  public void onTextMessage(String message, Session sender) {
    LOG.debug("Text message from {}: {}", sender.getId(), message);
    // Relay text to all other clients
    for (UserSession userSession : sessions) {
      var session = userSession.session;
      if (!session.equals(sender) && session.isOpen()) {
        try {
          session.getBasicRemote().sendText(message);
        } catch (IOException e) {
          LOG.error("Client error", e);
        }
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
        try {
          session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
        } catch (IOException e) {
          LOG.error("Client error", e);
        }
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

  @OnError
  public void onError(Session session, Throwable throwable) {
    LOG.error("Error on session {}: {}", session.getId(), throwable.getMessage());
  }

  private record UserSession(UUID user, Session session) {}

  private void closeSession(Session session, CloseCodes code, String reason) {
    try {
      session.close(new CloseReason(code, reason));
    } catch (Exception ignored) {
    }
  }
}
