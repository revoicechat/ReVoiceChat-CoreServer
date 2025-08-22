package fr.revoicechat.service.sse;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
class SseShutDownHook {
  private final TextualChatService textualChatService;

  public SseShutDownHook(final TextualChatService textualChatService) {this.textualChatService = textualChatService;}

  @PostConstruct
  public void registerHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(textualChatService::shutdownSseEmitters));
  }
}
