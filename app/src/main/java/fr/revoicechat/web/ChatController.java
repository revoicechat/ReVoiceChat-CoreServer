package fr.revoicechat.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.TextualChatService;

@RestController
@RequestMapping
public class ChatController {

  private final TextualChatService textualChatService;
  private final UserHolder userHolder;

  public ChatController(final TextualChatService textualChatService, final UserHolder userHolder) {
    this.textualChatService = textualChatService;
    this.userHolder = userHolder;
  }

  @GetMapping("/sse")
  public SseEmitter generateSseEmitter() {
    var user = userHolder.get();
    return textualChatService.register(user);
  }
}
