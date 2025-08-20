package fr.revoicechat.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.model.FileType;
import fr.revoicechat.model.MediaData;
import fr.revoicechat.model.MediaDataStatus;
import fr.revoicechat.model.MediaOrigin;
import fr.revoicechat.model.Message;
import fr.revoicechat.repository.MediaDataRepository;
import fr.revoicechat.repository.MessageRepository;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation.ActionType;
import fr.revoicechat.representation.message.MessageRepresentation.UserMessageRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.message.MessageValidation;
import fr.revoicechat.service.sse.TextualChatService;
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
 * {@link TextualChatService} to broadcast message-related events (additions, updates,
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
 * @see TextualChatService
 * @see RoomService
 */
@Service
public class MessageService {

  private final MessageRepository messageRepository;
  private final TextualChatService textualChatService;
  private final RoomService roomService;
  private final UserHolder userHolder;
  private final MediaDataRepository mediaDataRepository;
  private final MessageValidation messageValidation;

  public MessageService(final MessageRepository messageRepository, final TextualChatService textualChatService, final RoomService roomService, final UserHolder userHolder, final MediaDataRepository mediaDataRepository, final MessageValidation messageValidation) {
    this.messageRepository = messageRepository;
    this.textualChatService = textualChatService;
    this.roomService = roomService;
    this.userHolder = userHolder;
    this.mediaDataRepository = mediaDataRepository;
    this.messageValidation = messageValidation;
  }

  /**
   * Retrieves all messages for a given chat room.
   *
   * @param roomId the unique identifier of the chat room
   * @return list of messages in the room, possibly empty if no messages exist
   */
  @Transactional
  public List<MessageRepresentation> findAll(final UUID roomId) {
    return messageRepository.findByRoomId(roomId)
                            .map(message -> toRepresantation(message, null))
                            .toList();
  }

  @Transactional
  public Page<MessageRepresentation> getMessagesByRoom(UUID roomId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
    return messageRepository.findByRoomId(roomId, pageable).map(
        message -> toRepresantation(message, null)
    );
  }

  /**
   * Creates and persists a new message in the specified chat room.
   * Also notifies connected clients about the new message.
   *
   * @param roomId   the unique identifier of the chat room where the message will be added
   * @param creation the message data to create
   * @return a representation of the created message
   */
  public MessageRepresentation create(UUID roomId, CreatedMessageRepresentation creation) {
    messageValidation.isValid(creation);
    var room = roomService.read(roomId);
    var message = new Message();
    message.setId(UUID.randomUUID());
    message.setText(creation.text());
    message.setCreatedDate(LocalDateTime.now());
    message.setRoom(room);
    message.setUser(userHolder.get());
    creation.medias().stream().map(this::create).forEach(message::addMediaData);
    messageRepository.save(message);
    var representation = toRepresantation(message, ActionType.ADD);
    textualChatService.send(room.getId(), representation);
    return representation;
  }

  // TODO - move to a specific service
  private MediaData create(final CreatedMediaDataRepresentation creation) {
    MediaData mediaData = new MediaData();
    mediaData.setId(UUID.randomUUID());
    mediaData.setName(creation.name());
    // TODO - determine file type
    mediaData.setType(FileType.PICTURE);
    mediaData.setOrigin(MediaOrigin.ATTACHMENT);
    mediaData.setStatus(MediaDataStatus.DOWNLOADING);
    mediaDataRepository.save(mediaData);
    return mediaData;
  }

  /**
   * Retrieves the details of a specific message.
   *
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
   *
   * @param id       the unique identifier of the message to update
   * @param creation the new message content
   * @return a representation of the updated message
   * @throws ResourceNotFoundException if the message does not exist
   */
  public MessageRepresentation update(UUID id, CreatedMessageRepresentation creation) {
    var message = getMessage(id);
    message.setText(creation.text());
    messageRepository.save(message);
    var representation = toRepresantation(message, ActionType.MODIFY);
    textualChatService.send(message.getRoom().getId(), representation);
    return representation;
  }

  /**
   * Deletes a message from the repository and notifies connected clients.
   *
   * @param id the unique identifier of the message to delete
   * @return the UUID of the deleted message
   * @throws ResourceNotFoundException if the message does not exist
   */
  public UUID delete(UUID id) {
    var message = getMessage(id);
    var room = message.getRoom().getId();
    messageRepository.deleteById(id);
    textualChatService.send(room, new MessageRepresentation(id,
        null,
        room,
        null,
        null,
        ActionType.REMOVE,
        null));
    return id;
  }

  private Message getMessage(final UUID id) {
    return messageRepository
        .findById(id)
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
