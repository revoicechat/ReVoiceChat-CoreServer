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

import fr.revoicechat.model.Message;
import fr.revoicechat.model.Room;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.TextualChatService;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("room/{id}/chat")
public class ChatController {

  private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

  private final TextualChatService textualChatService;
  private final RoomService roomService;

  public ChatController(final TextualChatService textualChatService, final RoomService roomService) {
    this.textualChatService = textualChatService;
    this.roomService = roomService;
  }

  @GetMapping
  public Room get(@PathVariable("id") UUID roomId) {
    return roomService.get(roomId);
  }


  @GetMapping("/messages")
  public List<Message> messages(@PathVariable("id") UUID roomId) {
    return textualChatService.findAllMessage(roomId);
  }

  @GetMapping("sse")
  public SseEmitter generateSseEmitter(@PathVariable("id") UUID roomId) {
    return textualChatService.register(roomId);
  }

  @PutMapping
  public Message sendMessage(@PathVariable("id") UUID roomId, @RequestBody CreatedMessageRepresentation messageRepresentation) {
    var message = messageRepresentation.toEntity();
    textualChatService.send(roomId, message);
    return message;
  }

  @PostConstruct
  public void log() {
    LOG.info("Web controller for textual chat initialized");
  }
}
