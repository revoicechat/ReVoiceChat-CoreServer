package fr.revoicechat.core.web;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageFilterParams;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.message.MessagePageResult;
import fr.revoicechat.core.service.room.PrivateMessageService;
import fr.revoicechat.core.web.api.PrivateMessageController;
import fr.revoicechat.web.mapper.Mapper;

public class PrivateMessageControllerImpl implements PrivateMessageController {

  private final PrivateMessageService privateMessageService;
  private final MessageService messageService;
  private final MessagePageResult messagePageResult;

  public PrivateMessageControllerImpl(final PrivateMessageService privateMessageService, final MessageService messageService, final MessagePageResult messagePageResult) {
    this.privateMessageService = privateMessageService;
    this.messageService = messageService;
    this.messagePageResult = messagePageResult;
  }

  @Override
  public List<RoomRepresentation> findAll() {
    return Mapper.mapAll(privateMessageService.findAll());
  }

  @Override
  public RoomRepresentation get(final UUID id) {
    return Mapper.map(privateMessageService.get(id));
  }

  @Override
  public PageResult<MessageRepresentation> messages(final UUID roomId, final MessageFilterParams params) {
    return messagePageResult.getMessagesByRoom(roomId, params);
  }

  @Override
  public MessageRepresentation sendMessage(final UUID roomId, final CreatedMessageRepresentation representation) {
    return Mapper.map(messageService.create(roomId, representation));
  }
}
