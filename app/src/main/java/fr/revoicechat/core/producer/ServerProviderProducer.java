package fr.revoicechat.core.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;

import fr.revoicechat.core.config.RevoiceChatGlobalConfig;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.service.server.MonoServerProviderService;
import fr.revoicechat.core.service.server.MultiServerProviderService;
import fr.revoicechat.core.service.server.NewServerCreator;
import fr.revoicechat.core.service.server.ServerProviderService;

@ApplicationScoped
public class ServerProviderProducer {

  private final RevoiceChatGlobalConfig config;
  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;
  private final UserRepository userRepository;
  private final EntityManager entityManager;


  public ServerProviderProducer(RevoiceChatGlobalConfig config,
                                ServerRepository serverRepository,
                                NewServerCreator newServerCreator,
                                UserRepository userRepository,
                                EntityManager entityManager) {
    this.config = config;
    this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
    this.userRepository = userRepository;
    this.entityManager = entityManager;
  }

  @Produces
  @ApplicationScoped
  public ServerProviderService produceServerProviderService() {
    ServerProviderService service = switch (config.getSeverMode()) {
      case MONO_SERVER  -> new MonoServerProviderService(serverRepository, newServerCreator, userRepository);
      case MULTI_SERVER -> new MultiServerProviderService(serverRepository, userRepository, newServerCreator, entityManager);
    };
    service.canBeUsed();
    return service;
  }
}
