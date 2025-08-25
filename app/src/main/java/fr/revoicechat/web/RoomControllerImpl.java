package fr.revoicechat.web;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.model.Room;
import fr.revoicechat.repository.page.PageResult;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.service.MessageService;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.web.api.RoomController;

@RolesAllowed("USER") // only authenticated users
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
  public PageResult<MessageRepresentation> messages(UUID roomId, int page, int size) {
    return messageService.getMessagesByRoom(roomId, page, size);
  }

  @Override
  public MessageRepresentation sendMessage(UUID roomId, CreatedMessageRepresentation representation) {
    return messageService.create(roomId, representation);
  }
}
