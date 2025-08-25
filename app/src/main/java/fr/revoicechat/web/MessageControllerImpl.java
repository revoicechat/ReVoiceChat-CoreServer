package fr.revoicechat.web;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.service.MessageService;
import fr.revoicechat.web.api.MessageController;

@RolesAllowed("USER") // only authenticated users
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
}
