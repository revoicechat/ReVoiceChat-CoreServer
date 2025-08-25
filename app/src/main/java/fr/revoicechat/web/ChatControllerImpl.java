package fr.revoicechat.web;

import fr.revoicechat.representation.sse.SseData;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.sse.TextualChatService;
import fr.revoicechat.web.api.ChatController;
import io.smallrye.mutiny.Multi;

public class ChatControllerImpl implements ChatController {

  private final TextualChatService textualChatService;
  private final UserHolder userHolder;

  public ChatControllerImpl(final TextualChatService textualChatService, final UserHolder userHolder) {
    this.textualChatService = textualChatService;
    this.userHolder = userHolder;
  }
  @Override
  public Multi<SseData> generateSseEmitter() {
    var user = userHolder.get();
    return textualChatService.register(user.getId());
  }
}
