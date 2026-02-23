package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.UUID;

import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageFilterParams;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomPresenceRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.retriever.EntityByRoomIdRetriever;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.message.MessagePageResult;
import fr.revoicechat.core.technicaldata.RoomPresence;
import fr.revoicechat.core.web.api.RoomController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class RoomControllerImpl implements RoomController {

  private final RoomService roomService;
  private final MessageService messageService;
  private final MessagePageResult messagePageResult;

  public RoomControllerImpl(RoomService roomService,
                            MessageService messageService,
                            MessagePageResult messagePageResult) {
    this.roomService = roomService;
    this.messageService = messageService;
    this.messagePageResult = messagePageResult;
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_READ", retriever = EntityByRoomIdRetriever.class)
  public RoomRepresentation read(UUID roomId) {
    return Mapper.map(roomService.getRoom(roomId));
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_UPDATE", retriever = EntityByRoomIdRetriever.class)
  public RoomRepresentation update(UUID roomId, CreationRoomRepresentation representation) {
    return Mapper.map(roomService.update(roomId, representation));
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_DELETE", retriever = EntityByRoomIdRetriever.class)
  public UUID delete(UUID roomId) {
    return roomService.delete(roomId);
  }


  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_READ_MESSAGE", retriever = EntityByRoomIdRetriever.class)
  public PageResult<MessageRepresentation> messages(UUID roomId, MessageFilterParams params) {
    return messagePageResult.getMessagesByRoom(roomId, params);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_SEND_MESSAGE", retriever = EntityByRoomIdRetriever.class)
  public MessageRepresentation sendMessage(UUID roomId, CreatedMessageRepresentation representation) {
    return Mapper.map(messageService.create(roomId, representation));
  }

  @Override
  public RoomPresenceRepresentation fetchUsers(final UUID id) {
    return Mapper.map(new RoomPresence(roomService.getRoom(id)));
  }
}
