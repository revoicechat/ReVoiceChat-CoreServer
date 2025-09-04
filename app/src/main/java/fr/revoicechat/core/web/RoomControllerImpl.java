package fr.revoicechat.core.web;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomPresence;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.room.RoomPresenceService;
import fr.revoicechat.core.web.api.RoomController;

@RolesAllowed("USER") // only authenticated users
public class RoomControllerImpl implements RoomController {

  private final RoomService roomService;
  private final RoomPresenceService roomPresenceService;
  private final MessageService messageService;

  public RoomControllerImpl(RoomService roomService, RoomPresenceService roomPresenceService, MessageService messageService) {
    this.roomService = roomService;
    this.roomPresenceService = roomPresenceService;
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
    var sizeParam = size == 0 ? 50 : size;
    return messageService.getMessagesByRoom(roomId, page, sizeParam);
  }

  @Override
  public MessageRepresentation sendMessage(UUID roomId, CreatedMessageRepresentation representation) {
    return messageService.create(roomId, representation);
  }

  @Override
  public RoomPresence fetchUsers(final UUID id) {
    return roomPresenceService.get(id);
  }
}
