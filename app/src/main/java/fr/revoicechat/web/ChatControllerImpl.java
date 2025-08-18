package fr.revoicechat.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.TextualChatService;
import fr.revoicechat.web.api.ChatController;

@RestController
public class ChatControllerImpl implements ChatController {

  private final TextualChatService textualChatService;
  private final UserHolder userHolder;

  public ChatControllerImpl(final TextualChatService textualChatService, final UserHolder userHolder) {
    this.textualChatService = textualChatService;
    this.userHolder = userHolder;
  }

  @Override
  public SseEmitter generateSseEmitter() {
    var user = userHolder.get();
    return textualChatService.register(user.getId());
  }
}
