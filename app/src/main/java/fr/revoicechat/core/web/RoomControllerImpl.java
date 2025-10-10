package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.UUID;

import fr.revoicechat.core.service.message.MessagePageResult;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomPresence;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.retriever.EntityByRoomIdRetriever;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.room.RoomPresenceService;
import fr.revoicechat.core.web.api.RoomController;
import fr.revoicechat.risk.RisksMembershipData;

@RolesAllowed(ROLE_USER)
public class RoomControllerImpl implements RoomController {

  private final RoomService roomService;
  private final RoomPresenceService roomPresenceService;
  private final MessageService messageService;
  private final MessagePageResult messagePageResult;

  public RoomControllerImpl(RoomService roomService,
                            RoomPresenceService roomPresenceService,
                            MessageService messageService,
                            MessagePageResult messagePageResult) {
    this.roomService = roomService;
    this.roomPresenceService = roomPresenceService;
    this.messageService = messageService;
    this.messagePageResult = messagePageResult;
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_READ", retriever = EntityByRoomIdRetriever.class)
  public RoomRepresentation read(UUID roomId) {
    return roomService.read(roomId);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_UPDATE", retriever = EntityByRoomIdRetriever.class)
  public RoomRepresentation update(UUID roomId, CreationRoomRepresentation representation) {
    return roomService.update(roomId, representation);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_DELETE", retriever = EntityByRoomIdRetriever.class)
  public UUID delete(UUID roomId) {
    return roomService.delete(roomId);
  }


  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_READ_MESSAGE", retriever = EntityByRoomIdRetriever.class)
  public PageResult<MessageRepresentation> messages(UUID roomId, int page, int size) {
    var sizeParam = size == 0 ? 50 : size;
    return messagePageResult.getMessagesByRoom(roomId, page, sizeParam);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_SEND_MESSAGE", retriever = EntityByRoomIdRetriever.class)
  public MessageRepresentation sendMessage(UUID roomId, CreatedMessageRepresentation representation) {
    return messageService.create(roomId, representation);
  }

  @Override
  public RoomPresence fetchUsers(final UUID id) {
    return roomPresenceService.get(id);
  }
}
