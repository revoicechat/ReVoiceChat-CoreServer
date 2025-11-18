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

/**
 * WebSocket endpoint for voice chat room functionality.
 *
 * <p>This endpoint manages real-time voice communication sessions for users in voice rooms.
 * It handles connection lifecycle, message routing between participants, and permission
 * validation for sending and receiving voice data.</p>
 *
 * <p>The endpoint is available at {@code /voice/{roomId}} where {@code roomId} is the
 * UUID of the voice room to join.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic disconnection of previous sessions when a user connects again</li>
 *   <li>Permission-based message routing (send/receive risks)</li>
 *   <li>Room membership validation</li>
 *   <li>Real-time notifications to room participants on join/leave events</li>
 * </ul>
 *
 * @see VoiceSession
 * @see WebSocketService
 * @see ConnectedUserRetriever
 */
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

  /**
   * Called when a client opens a WebSocket connection to a voice room.
   *
   * <p>Delegates to {@link WebSocketService#onOpen} for authentication,
   * then calls {@link #handleOpen} for room-specific logic.</p>
   *
   * @param session the WebSocket session being opened
   * @param roomIdAsString the voice room ID from the URL path
   */
  @OnOpen
  @SuppressWarnings("unused") // call by websocket listener
  public void onOpen(Session session, @PathParam("roomId") String roomIdAsString) {
    webSocketService.onOpen(session, token -> handleOpen(session, token, roomIdAsString));
  }

  /**
   * Handles the actual connection logic including database operations and notifications.
   *
   * <p>This method runs inside a {@link ManagedExecutor} with a transactional context.
   * It performs the following validations and operations:</p>
   * <ol>
   *   <li>Validates the user token</li>
   *   <li>Closes any existing session for the user (preventing duplicate connections)</li>
   *   <li>Verifies the room accepts voice chat</li>
   *   <li>Checks user permissions to join the room</li>
   *   <li>Adds the user to the active sessions</li>
   *   <li>Notifies other room participants of the new user</li>
   * </ol>
   *
   * @param session the WebSocket session
   * @param token the authentication token
   * @param roomIdAsString the voice room ID as a string
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

  /**
   * Closes any existing session for the given user.
   *
   * <p>This ensures a user can only have one active voice connection at a time.</p>
   *
   * @param user the authenticated user
   */
  private void closeOldSession(final AuthenticatedUser user) {
    var existingSession = getExistingSession(user.getId());
    if (existingSession != null) {
      handleCloseSession(existingSession);
    }
  }

  /**
   * Called when a text message is received from a client.
   *
   * <p>Routes the message to appropriate receivers based on room membership
   * and permissions.</p>
   *
   * @param message the text message content
   * @param sender the session that sent the message
   */
  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(String message, Session sender) {
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  /**
   * Called when a binary message (voice data) is received from a client.
   *
   * <p>Routes the message to appropriate receivers based on room membership
   * and permissions.</p>
   *
   * @param message the binary message content
   * @param sender the session that sent the message
   */
  @OnMessage
  @SuppressWarnings("unused") // call by websocket listener
  public void onMessage(byte[] message, Session sender) {
    webSocketService.onMessage(message, sender, getReceiver(sender));
  }

  /**
   * Determines which sessions should receive a message from the sender.
   *
   * <p>Recipients are filtered based on:</p>
   * <ul>
   *   <li>Being in the same voice room</li>
   *   <li>Sender having permission to send messages</li>
   *   <li>Receiver having permission to receive messages</li>
   *   <li>Not being the sender themselves</li>
   * </ul>
   *
   * @param sender the session sending the message
   * @return stream of sessions that should receive the message
   */
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

  /**
   * Called when a WebSocket connection is closed.
   *
   * <p>Enqueues the close handler for async execution to properly
   * clean up the session and notify other participants.</p>
   *
   * @param session the session being closed
   */
  @OnClose
  @SuppressWarnings("unused") // call by websocket listener
  public void onClose(Session session) {
    sessions.stream().filter(voiceSession -> voiceSession.is(session))
            .findFirst()
            .ifPresent(voiceSession -> webSocketService.closeSession(voiceSession.user(), session, () -> handleCloseSession(voiceSession)));
  }

  /**
   * Handles the actual session close logic with database operations and notifications.
   *
   * <p>This method:</p>
   * <ul>
   *   <li>Logs the disconnection</li>
   *   <li>Notifies other room participants that the user has left</li>
   *   <li>Closes the WebSocket session</li>
   *   <li>Removes the session from the active sessions set</li>
   * </ul>
   *
   * @param voiceSession the voice session being closed
   */
  @Transactional
  public void handleCloseSession(final VoiceSession voiceSession) {
    LOG.info("Client disconnected: {}", voiceSession);
    Notification.of(new VoiceLeavingNotification(voiceSession.user(), voiceSession.room())).sendTo(roomUserFinder.find(voiceSession.room()));
    webSocketService.closeSession(voiceSession.session(), CloseCodes.NORMAL_CLOSURE, "Client disconnected");
    sessions.remove(voiceSession);
  }

  /**
   * Finds an existing session for a given user ID.
   *
   * @param user the user ID to search for
   * @return the user's voice session, or null if not found
   */
  private VoiceSession getExistingSession(UUID user) {
    return sessions.stream().filter(voiceSession -> voiceSession.user().equals(user))
                   .findFirst()
                   .orElse(null);
  }

  /**
   * Finds an existing session matching a given WebSocket session.
   *
   * @param session the WebSocket session to search for
   * @return the corresponding voice session, or null if not found
   */
  private VoiceSession getExistingSession(Session session) {
    return sessions.stream().filter(voiceSession -> voiceSession.is(session))
                   .findFirst()
                   .orElse(null);
  }

  /**
   * Called when an error occurs on a WebSocket connection.
   *
   * <p>Delegates error handling to {@link WebSocketService}.</p>
   *
   * @param session the session where the error occurred
   * @param throwable the error
   */
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
