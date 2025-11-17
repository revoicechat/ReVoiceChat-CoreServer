package fr.revoicechat.live.voice.socket;

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

import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.representation.UserNotificationRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.model.AuthenticatedUser;
import fr.revoicechat.live.voice.notification.VoiceJoiningNotification;
import fr.revoicechat.live.voice.notification.VoiceLeavingNotification;
import fr.revoicechat.live.common.service.DiscussionRiskService;
import fr.revoicechat.live.voice.service.VoiceRoomPredicate;
import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import fr.revoicechat.live.voice.service.VoiceRoomUserFinder;
import fr.revoicechat.live.common.socket.WebSocketAuthConfigurator;
import fr.revoicechat.live.common.socket.WebSocketService;

@ServerEndpoint(value = "/voice/{roomId}", configurator = WebSocketAuthConfigurator.class)
@ApplicationScoped
public class VoiceWebSocket implements ConnectedUserRetriever {
  private static final Logger LOG = LoggerFactory.getLogger(VoiceWebSocket.class);

  // Thread-safe set of connected sessions
  private static final Set<VoiceSession> sessions = ConcurrentHashMap.newKeySet();

  private final WebSocketService webSocketService;
  private final UserHolder userHolder;
  private final VoiceRoomUserFinder roomUserFinder;
  private final VoiceRoomPredicate voiceRoomPredicate;
  private final DiscussionRiskService discussionRiskService;

  public VoiceWebSocket(final WebSocketService webSocketService,
                        final UserHolder userHolder,
                        final VoiceRoomUserFinder roomUserFinder,
                        final VoiceRoomPredicate voiceRoomPredicate,
                        final DiscussionRiskService discussionRiskService) {
    this.webSocketService = webSocketService;
    this.userHolder = userHolder;
    this.roomUserFinder = roomUserFinder;
    this.voiceRoomPredicate = voiceRoomPredicate;
    this.discussionRiskService = discussionRiskService;
  }

  @OnOpen
  @SuppressWarnings("unused") // call by websocket listener
  public void onOpen(Session session, @PathParam("roomId") String roomIdAsString) {
    webSocketService.onOpen(session, token -> handleOpen(session, token, roomIdAsString));
  }

  /**
   * The actual heavy open logic (DB, session, notification).
   * Runs inside the ManagedExecutor with @Transactional context.
   */
  @Transactional
  void handleOpen(Session session, String token, String roomIdAsString) {
    UUID roomId = UUID.fromString(roomIdAsString);
    var user = userHolder.get(token);
    if (user == null) {
      webSocketService.closeSession(session, CloseCodes.VIOLATED_POLICY, "Invalid token");
      return;
    }
    closeOldSession(user);
    if (!voiceRoomPredicate.isVoiceRoom(roomId)) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "Selected room cannot accept websocket chat type");
      return;
    }
    var voiceRisk = discussionRiskService.getVoiceRisks(roomId, user.getId());
    if (!voiceRisk.join()) {
      webSocketService.closeSession(session, CloseCodes.CANNOT_ACCEPT, "User is not authorized to join voice room");
      return;
    }
    LOG.info("WebSocket connected as user {}", user.getId());
    sessions.add(new VoiceSession(user.getId(), roomId, voiceRisk, session));
    var data = new VoiceJoiningNotification(new UserNotificationRepresentation(user.getId(), user.getDisplayName()), roomId);
    Notification.of(data).sendTo(roomUserFinder.find(roomId));
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
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(byte[] message, Session sender) {
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  private Stream<Session> getReceiver(Session sender) {
    var current = getExistingSession(sender);
    if (current == null || !current.risks().send()) {
      return Stream.empty();
    }
    return sessions.stream()
                   .filter(voiceSession -> voiceSession.room().equals(current.room()))
                   .filter(voiceSession -> voiceSession.risks().receive())
                   .map(VoiceSession::session)
                   .filter(session -> !session.getId().equals(sender.getId()));
  }

  @OnClose
  @SuppressWarnings("unused") // call by websocket listener
  public void onClose(Session session) {
    sessions.stream().filter(voiceSession -> voiceSession.is(session))
            .findFirst()
            .ifPresent(voiceSession -> webSocketService.closeSession(voiceSession.user(), session, () -> handleCloseSession(voiceSession)));
  }

  @Transactional
  public void handleCloseSession(final VoiceSession voiceSession) {
    LOG.info("Client disconnected: {}", voiceSession.sessionId());
    Notification.of(new VoiceLeavingNotification(voiceSession.user(), voiceSession.room())).sendTo(roomUserFinder.find(voiceSession.room()));
    webSocketService.closeSession(voiceSession.session(), CloseCodes.NORMAL_CLOSURE, "Client disconnected");
    sessions.remove(voiceSession);
  }

  private VoiceSession getExistingSession(UUID user) {
    return sessions.stream().filter(voiceSession -> voiceSession.user().equals(user))
                   .findFirst()
                   .orElse(null);
  }

  private VoiceSession getExistingSession(Session session) {
    return sessions.stream().filter(voiceSession -> voiceSession.is(session))
                   .findFirst()
                   .orElse(null);
  }

  @OnError
  @SuppressWarnings("unused") // call by websocket listener
  public void onError(Session session, Throwable throwable) {
    webSocketService.onError(session, throwable);
  }

  @Override
  public Stream<UUID> getConnectedUsers(UUID room) {
    return sessions.stream()
                   .filter(voiceSession -> voiceSession.room().equals(room))
                   .map(VoiceSession::user);
  }

  @Override
  public UUID getRoomForUser(final UUID user) {
    return sessions.stream()
                   .filter(voiceSession -> voiceSession.user().equals(user))
                   .findFirst()
                   .map(VoiceSession::room)
                   .orElse(null);
  }
}
