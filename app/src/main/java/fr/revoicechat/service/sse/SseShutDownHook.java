package fr.revoicechat.service.sse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
class SseShutDownHook {
  private final TextualChatService textualChatService;

  public SseShutDownHook(final TextualChatService textualChatService) {this.textualChatService = textualChatService;}

  @PostConstruct
  public void registerHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(textualChatService::shutdownSseEmitters));
  }
}
