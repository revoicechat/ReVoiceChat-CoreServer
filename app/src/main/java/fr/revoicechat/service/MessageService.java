package fr.revoicechat.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.revoicechat.model.Message;
import fr.revoicechat.repository.MessageRepository;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation.ActionType;
import jakarta.transaction.Transactional;

@Service
public class MessageService {

  private final MessageRepository messageRepository;
  private final TextualChatService textualChatService;
  private final RoomService roomService;

  public MessageService(final MessageRepository messageRepository, final TextualChatService textualChatService, final RoomService roomService) {
    this.messageRepository = messageRepository;
    this.textualChatService = textualChatService;
    this.roomService = roomService;
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

  public MessageRepresentation create(UUID roomId, CreatedMessageRepresentation creation) {
    var room = roomService.get(roomId);
    var message = creation.toEntity();
    message.setRoom(room);
    messageRepository.save(message);
    var representation = toRepresantation(message, ActionType.ADD);
    textualChatService.send(room.getId(), representation);
    return representation;
  }

  public MessageRepresentation read(UUID id) {
    var message = messageRepository.findById(id).orElseThrow();
    return toRepresantation(message, null);
  }

  public MessageRepresentation update(UUID id, CreatedMessageRepresentation creation) {
    var message = messageRepository.findById(id).orElseThrow();
    message.setText(creation.text());
    messageRepository.save(message);
    var representation = toRepresantation(message, ActionType.MODIFY);
    textualChatService.send(message.getRoom().getId(), representation);
    return representation;
  }

  public UUID delete(UUID id) {
    var message = messageRepository.findById(id).orElseThrow();
    var room = message.getRoom().getId();
    messageRepository.deleteById(id);
    textualChatService.send(room, new MessageRepresentation(id, null, room, null, ActionType.REMOVE));
    return id;
  }

  private MessageRepresentation toRepresantation(final Message message, final ActionType actionType) {
    return new MessageRepresentation(
        message.getId(),
        message.getText(),
        message.getRoom().getId(),
        message.getCreatedDate(),
        actionType
    );
  }
}
