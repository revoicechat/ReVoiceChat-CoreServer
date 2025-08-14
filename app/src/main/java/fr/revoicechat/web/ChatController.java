package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.revoicechat.model.Room;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.service.MessageService;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.TextualChatService;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("room/{id}")
public class ChatController {

  private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

  private final TextualChatService textualChatService;
  private final RoomService roomService;
  private final MessageService messageService;

  public ChatController(final TextualChatService textualChatService, final RoomService roomService, final MessageService messageService) {
    this.textualChatService = textualChatService;
    this.roomService = roomService;
    this.messageService = messageService;
  }

  @GetMapping
  public Room get(@PathVariable("id") UUID roomId) {
    return roomService.get(roomId);
  }

  @GetMapping("/message")
  public List<MessageRepresentation> messages(@PathVariable("id") UUID roomId) {
    return messageService.findAll(roomId);
  }

  @GetMapping("/sse")
  public SseEmitter generateSseEmitter(@PathVariable("id") UUID roomId) {
    return textualChatService.register(roomId);
  }

  @PutMapping("/message")
  public MessageRepresentation sendMessage(@PathVariable("id") UUID roomId, @RequestBody CreatedMessageRepresentation messageRepresentation) {
    return messageService.create(roomId, messageRepresentation);
  }

  @PostConstruct
  public void log() {
    LOG.info("Web controller for textual chat initialized");
  }
}
