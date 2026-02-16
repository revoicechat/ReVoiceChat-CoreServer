package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.web.api.MessageController;

@RolesAllowed(ROLE_USER)
public class MessageControllerImpl implements MessageController {

  private final MessageService messageService;

  public MessageControllerImpl(final MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public MessageRepresentation read(UUID id) {
    return messageService.read(id);
  }

  @Override
  public MessageRepresentation update(UUID id, CreatedMessageRepresentation representation) {
    return messageService.update(id, representation);
  }

  @Override
  public UUID delete(UUID id) {
    return messageService.delete(id);
  }

  @Override
  public MessageRepresentation addReaction(final UUID id, final String emoji) {
    return messageService.addReaction(id, emoji);
  }
}
