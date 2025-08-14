package fr.revoicechat.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.service.MessageService;

@RestController
@RequestMapping("message/{id}")
public class MessageController {

  private final MessageService messageService;

  public MessageController(final MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping
  public MessageRepresentation read(@PathVariable("id") UUID id) {
    return messageService.read(id);
  }

  @PatchMapping
  public MessageRepresentation update(@PathVariable("id") UUID id, @RequestBody CreatedMessageRepresentation representation) {
    return messageService.update(id, representation);
  }

  @DeleteMapping
  public UUID delete(@PathVariable("id") UUID id) {
    return messageService.delete(id);
  }
}
