package fr.revoicechat.service.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;
import jakarta.transaction.Transactional;

/**
 *
 */
@Service
public class MonoServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(ServerProviderService.class);

  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;

  public MonoServerProviderService(final ServerRepository serverRepository, final NewServerCreator newServerCreator) {this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
  }

  /** This can only be used if we have only one {@link Server} */
  @Override
  public void canBeUsed() {
    if (serverRepository.count() > 1) {
      LOG.error("Mono server mode : error");
      throwEx();
    }
    LOG.info("Mono server mode : can be used");
  }

  @Override
  @Transactional
  public List<Server> getServers() {
    var servers = serverRepository.findAll();
    if (servers.size() > 1) {
      throwEx();
    } else if (servers.size() == 1) {
      return servers;
    }
    Server server = newServerCreator.create(new Server());
    return List.of(server);
  }

  private static void throwEx() {
    throw new IllegalStateException("""
        Yous current application cant be run in mono server \
        because you have more thant one existing server""");
  }
}
