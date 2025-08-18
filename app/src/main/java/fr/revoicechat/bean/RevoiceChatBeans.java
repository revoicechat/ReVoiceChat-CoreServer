package fr.revoicechat.bean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.revoicechat.config.RevoiceChatGlobalConfig;
import fr.revoicechat.service.server.MonoServerProviderService;
import fr.revoicechat.service.server.MultiServerProviderService;
import fr.revoicechat.service.server.ServerProviderService;

@Configuration
public class RevoiceChatBeans {

  private final ApplicationContext applicationContext;

  public RevoiceChatBeans(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Bean
  public ServerProviderService serverProviderService(RevoiceChatGlobalConfig config) {
    var service = switch (config.getSeverMode()) {
      case MONO_SERVER  -> applicationContext.getBean(MonoServerProviderService.class);
      case MULTI_SERVER -> applicationContext.getBean(MultiServerProviderService.class);
    };
    service.canBeUsed();
    return service;
  }
}
