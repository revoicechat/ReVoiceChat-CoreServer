package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.UUID;

import fr.revoicechat.core.notification.MessageNotification;
import fr.revoicechat.core.representation.MessageRepresentation;
import fr.revoicechat.core.service.message.MessageService;
import fr.revoicechat.core.technicaldata.message.NewMessage;
import fr.revoicechat.core.web.api.MessageController;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class MessageControllerImpl implements MessageController {

  private final MessageService messageService;

  public MessageControllerImpl(final MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public MessageRepresentation read(UUID id) {
    return Mapper.map(messageService.getMessage(id));
  }

  @Override
  public MessageRepresentation update(UUID id, NewMessage newMessage) {
    var message = messageService.update(id, newMessage);
    MessageNotification.update(message);
    return Mapper.map(message);
  }

  @Override
  public UUID delete(UUID id) {
    var message = messageService.delete(id);
    MessageNotification.delete(message);
    return message.getId();
  }

  @Override
  public MessageRepresentation addReaction(final UUID id, final String emoji) {
    var message = messageService.addReaction(id, emoji);
    MessageNotification.update(message);
    return Mapper.map(message);
  }
}
