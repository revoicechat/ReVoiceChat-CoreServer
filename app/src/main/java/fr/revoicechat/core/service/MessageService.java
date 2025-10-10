package fr.revoicechat.core.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.repository.MessageRepository;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageNotification;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.core.service.emote.EmoteService;
import fr.revoicechat.core.service.media.MediaDataService;
import fr.revoicechat.core.service.message.MessageValidation;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.representation.UserNotificationRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Service layer for managing chat messages within rooms.
 * <p>
 * This service provides operations to create, retrieve, update, and delete messages.
 * It acts as an intermediary between the {@link MessageRepository} (data access layer)
 * and higher-level components such as controllers or WebSocket endpoints.
 * </p>
 * <p>
 * In addition to persisting messages, this service interacts with the
 * {@link Notification} to broadcast message-related events (additions, updates,
 * removals) to connected clients in real-time.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Manage message persistence for specific chat rooms</li>
 *   <li>Transform domain entities into {@link MessageRepresentation} objects</li>
 *   <li>Notify clients about message events via the textual chat service</li>
 * </ul>
 *
 * @see MessageRepository
 * @see Notification
 * @see RoomService
 */
@ApplicationScoped
public class MessageService {

  private final EntityManager entityManager;
  private final RoomService roomService;
  private final UserHolder userHolder;
  private final MessageValidation messageValidation;
  private final MediaDataService mediaDataService;
  private final RoomUserFinder roomUserFinder;
  private final EmoteService emoteService;

  public MessageService(EntityManager entityManager,
                        RoomService roomService,
                        UserHolder userHolder,
                        MessageValidation messageValidation,
                        MediaDataService mediaDataService,
                        RoomUserFinder roomUserFinder,
                        EmoteService emoteService) {
    this.entityManager = entityManager;
    this.roomService = roomService;
    this.userHolder = userHolder;
    this.messageValidation = messageValidation;
    this.mediaDataService = mediaDataService;
    this.roomUserFinder = roomUserFinder;
    this.emoteService = emoteService;
  }

  /**
   * Creates and persists a new message in the specified chat room.
   * Also notifies connected clients about the new message.
   *
   * @param roomId   the unique identifier of the chat room where the message will be added
   * @param creation the message data to create
   * @return a representation of the created message
   */
  @Transactional
  public MessageRepresentation create(UUID roomId, CreatedMessageRepresentation creation) {
    messageValidation.isValid(creation);
    var room = roomService.getRoom(roomId);
    var message = new Message();
    message.setId(UUID.randomUUID());
    message.setText(creation.text());
    message.setCreatedDate(LocalDateTime.now());
    message.setRoom(room);
    message.setUser(userHolder.get());
    creation.medias().stream().map(mediaDataService::create).forEach(message::addMediaData);
    entityManager.persist(message);
    var representation = toRepresentation(message);
    Notification.of(new MessageNotification(representation, NotificationActionType.ADD)).sendTo(roomUserFinder.find(room.getId()));
    return representation;
  }

  /**
   * Retrieves the details of a specific message.
   *
   * @param id the unique identifier of the message
   * @return a representation of the message
   * @throws ResourceNotFoundException if the message does not exist
   */
  @Transactional
  public MessageRepresentation read(UUID id) {
    var message = getMessage(id);
    return toRepresentation(message);
  }


  /**
   * Updates the content of an existing message and notifies connected clients.
   *
   * @param id       the unique identifier of the message to update
   * @param creation the new message content
   * @return a representation of the updated message
   * @throws ResourceNotFoundException if the message does not exist
   */
  @Transactional
  public MessageRepresentation update(UUID id, CreatedMessageRepresentation creation) {
    var message = getMessage(id);
    message.setText(creation.text());
    entityManager.persist(message);
    var representation = toRepresentation(message);
    Notification.of(new MessageNotification(representation, NotificationActionType.MODIFY)).sendTo(roomUserFinder.find(message.getRoom().getId()));
    return representation;
  }

  /**
   * Deletes a message from the repository and notifies connected clients.
   *
   * @param id the unique identifier of the message to delete
   * @return the UUID of the deleted message
   * @throws ResourceNotFoundException if the message does not exist
   */
  @Transactional
  public UUID delete(UUID id) {
    var message = getMessage(id);
    var room = message.getRoom().getId();
    entityManager.remove(message);
    var deletedMessage = new MessageNotification(new MessageRepresentation(id, null, room, null, null, null, null), NotificationActionType.REMOVE);
    Notification.of(deletedMessage).sendTo(roomUserFinder.find(room));
    return id;
  }

  private Message getMessage(final UUID id) {
    return Optional.ofNullable(entityManager.find(Message.class, id))
                   .orElseThrow(() -> new ResourceNotFoundException(Message.class, id));
  }

  public MessageRepresentation toRepresentation(final Message message) {
    var emotes = Stream.of(emoteService.getAll(message.getUser().getId()),
                           emoteService.getAll(message.getRoom().getServer().getId()))
                       .flatMap(Collection::stream)
                       .toList();
    return new MessageRepresentation(
        message.getId(),
        message.getText(),
        message.getRoom().getId(),
        new UserNotificationRepresentation(message.getUser().getId(), message.getUser().getDisplayName()),
        message.getCreatedDate().atOffset(ZoneOffset.UTC),
        message.getMediaDatas().stream()
               .map(mediaDataService::toRepresentation).toList(),
        emotes
    );
  }
}
