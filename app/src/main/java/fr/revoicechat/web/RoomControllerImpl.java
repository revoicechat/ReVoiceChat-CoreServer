package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.service.MessageService;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.web.api.RoomController;

@RestController
public class RoomControllerImpl implements RoomController {

  private final RoomService roomService;
  private final MessageService messageService;

  public RoomControllerImpl(final RoomService roomService, final MessageService messageService) {
    this.roomService = roomService;
    this.messageService = messageService;
  }

  @Override
  public Room read(UUID roomId) {
    return roomService.read(roomId);
  }

  @Override
  public Room update(UUID roomId, RoomRepresentation representation) {
    return roomService.update(roomId, representation);
  }

  @Override
  public UUID delete(UUID roomId) {
    return roomService.delete(roomId);
  }

  @Override
  public List<MessageRepresentation> messages(UUID roomId) {
    return messageService.findAll(roomId);
  }

  @Override
  public MessageRepresentation sendMessage(UUID roomId, CreatedMessageRepresentation representation) {
    return messageService.create(roomId, representation);
  }
}
