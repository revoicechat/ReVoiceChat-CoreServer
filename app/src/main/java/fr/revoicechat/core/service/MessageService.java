package fr.revoicechat.core.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.repository.MessageRepository;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation.ActionType;
import fr.revoicechat.core.representation.message.MessageRepresentation.UserMessageRepresentation;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.service.media.MediaDataService;
import fr.revoicechat.core.service.message.MessageValidation;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.model.Notification;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.notification.service.NotificationSender;
import fr.revoicechat.notification.service.NotificationService;

/**
 * Service layer for managing chat messages within rooms.
 * <p>
 * This service provides operations to create, retrieve, update, and delete messages.
 * It acts as an intermediary between the {@link MessageRepository} (data access layer)
 * and higher-level components such as controllers or WebSocket endpoints.
 * </p>
 * <p>
 * In addition to persisting messages, this service interacts with the
 * {@link NotificationService} to broadcast message-related events (additions, updates,
 * removals) to connected clients in real-time.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Manage message persistence for specific chat rooms</li>
 *   <li>Transform domain entities into {@link MessageRepresentation} objects</li>
 *   <li>Notify clients about message events via the textual chat service</li>
 * </ul>
 * @see MessageRepository
 * @see NotificationService
 * @see RoomService
 */
@ApplicationScoped
public class MessageService {

  private final EntityManager entityManager;
  private final MessageRepository messageRepository;
  private final NotificationSender notificationSender;
  private final RoomService roomService;
  private final UserHolder userHolder;
  private final MessageValidation messageValidation;
  private final MediaDataService mediaDataService;
  private final RoomUserFinder roomUserFinder;

  public MessageService(EntityManager entityManager,
                        MessageRepository messageRepository,
                        NotificationSender notificationSender,
                        RoomService roomService,
                        UserHolder userHolder,
                        MessageValidation messageValidation,
                        MediaDataService mediaDataService,
                        RoomUserFinder roomUserFinder) {
    this.entityManager = entityManager;
    this.messageRepository = messageRepository;
    this.notificationSender = notificationSender;
    this.roomService = roomService;
    this.userHolder = userHolder;
    this.messageValidation = messageValidation;
    this.mediaDataService = mediaDataService;
    this.roomUserFinder = roomUserFinder;
  }

  /**
   * Retrieves all messages for a given chat room.
   * @param roomId the unique identifier of the chat room
   * @return list of messages in the room, possibly empty if no messages exist
   */
  @Transactional
  public PageResult<MessageRepresentation> getMessagesByRoom(UUID roomId, int page, int size) {
    var pageResult = messageRepository.findByRoomId(roomId, page, size);
    return new PageResult<>(pageResult.content()
                                      .stream()
                                      .map(message -> toRepresantation(message, null))
                                      .toList(),
                            pageResult.pageNumber(), pageResult.pageSize(), pageResult.totalElements());
  }

  /**
   * Creates and persists a new message in the specified chat room.
   * Also notifies connected clients about the new message.
   * @param roomId   the unique identifier of the chat room where the message will be added
   * @param creation the message data to create
   * @return a representation of the created message
   */
  @Transactional
  public MessageRepresentation create(UUID roomId, CreatedMessageRepresentation creation) {
    messageValidation.isValid(creation);
    var room = roomService.read(roomId);
    var message = new Message();
    message.setId(UUID.randomUUID());
    message.setText(creation.text());
    message.setCreatedDate(LocalDateTime.now());
    message.setRoom(room);
    message.setUser(userHolder.get());
    creation.medias().stream().map(mediaDataService::create).forEach(message::addMediaData);
    entityManager.persist(message);
    var representation = toRepresantation(message, ActionType.ADD);
    notificationSender.send(Notification.forUsers(roomUserFinder.find(room.getId()))
                                        .withData(NotificationType.ROOM_MESSAGE, representation));
    return representation;
  }

  /**
   * Retrieves the details of a specific message.
   * @param id the unique identifier of the message
   * @return a representation of the message
   * @throws ResourceNotFoundException if the message does not exist
   */
  public MessageRepresentation read(UUID id) {
    var message = getMessage(id);
    return toRepresantation(message, null);
  }


  /**
   * Updates the content of an existing message and notifies connected clients.
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
    var representation = toRepresantation(message, ActionType.MODIFY);
    notificationSender.send(Notification.forUsers(roomUserFinder.find(message.getRoom().getId()))
                                        .withData(NotificationType.ROOM_MESSAGE, representation));
    return representation;
  }

  /**
   * Deletes a message from the repository and notifies connected clients.
   * @param id the unique identifier of the message to delete
   * @return the UUID of the deleted message
   * @throws ResourceNotFoundException if the message does not exist
   */
  @Transactional
  public UUID delete(UUID id) {
    var message = getMessage(id);
    var room = message.getRoom().getId();
    entityManager.remove(message);
    notificationSender.send(Notification.forUsers(roomUserFinder.find(room))
                                        .withData(NotificationType.ROOM_MESSAGE,
                                                  new MessageRepresentation(id, null, room, null, null, ActionType.REMOVE, null)));
    return id;
  }

  private Message getMessage(final UUID id) {
    return Optional.ofNullable(entityManager.find(Message.class, id))
                   .orElseThrow(() -> new ResourceNotFoundException(Message.class, id));
  }

  private MessageRepresentation toRepresantation(final Message message, final ActionType actionType) {
    return new MessageRepresentation(
        message.getId(),
        message.getText(),
        message.getRoom().getId(),
        new UserMessageRepresentation(
            message.getUser().getId(),
            message.getUser().getDisplayName()
        ),
        message.getCreatedDate().atOffset(ZoneOffset.UTC),
        actionType,
        List.of()
    );
  }
}
