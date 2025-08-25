package fr.revoicechat.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import fr.revoicechat.config.RevoiceChatGlobalConfig;
import fr.revoicechat.repository.ServerRepository;
import fr.revoicechat.repository.UserRepository;
import fr.revoicechat.service.server.MonoServerProviderService;
import fr.revoicechat.service.server.MultiServerProviderService;
import fr.revoicechat.service.server.NewServerCreator;
import fr.revoicechat.service.server.ServerProviderService;

@ApplicationScoped
public class ServerProviderProducer {

  private final RevoiceChatGlobalConfig config;
  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;
  private final UserRepository userRepository;


  public ServerProviderProducer(final RevoiceChatGlobalConfig config,
                                ServerRepository serverRepository,
                                NewServerCreator newServerCreator,
                                UserRepository userRepository) {
    this.config = config;
    this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
    this.userRepository = userRepository;
  }

  @Produces
  @ApplicationScoped
  public ServerProviderService produceServerProviderService() {
    ServerProviderService service = switch (config.getSeverMode()) {
      case MONO_SERVER  -> new MonoServerProviderService(serverRepository, newServerCreator, userRepository);
      case MULTI_SERVER -> new MultiServerProviderService(serverRepository, userRepository, newServerCreator);
    };
    service.canBeUsed();
    return service;
  }
}
