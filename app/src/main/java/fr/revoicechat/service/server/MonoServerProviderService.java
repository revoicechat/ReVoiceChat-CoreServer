package fr.revoicechat.service.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;
import jakarta.transaction.Transactional;

/**
 * {@link ServerProviderService} implementation for single-server mode.
 * <p>
 * In this mode, only one {@link Server} may exist in the system.
 * If no server is found, a new one is automatically created.
 */
@Service
public class MonoServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(MonoServerProviderService.class);

  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;

  public MonoServerProviderService(final ServerRepository serverRepository, final NewServerCreator newServerCreator) {this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
  }

  /**
   * Ensures that the application is in a valid single-server state.
   * <p>
   * This method throws an exception if more than one server exists.
   *
   * @throws IllegalStateException if more than one server exists
   */
  @Override
  public void canBeUsed() {
    if (serverRepository.count() > 1) {
      LOG.error("Mono server mode : error");
      throwEx();
    }
    LOG.info("Mono server mode : can be used");
  }

  /**
   * Returns the single server available in the system, creating one if none exists.
   * <p>
   * Throws an exception if more than one server is present.
   *
   * @return a list containing the single server
   * @throws IllegalStateException if more than one server exists
   */
  @Override
  @Transactional
  public List<Server> getServers() {
    var servers = serverRepository.findAll();
    if (servers.size() > 1) {
      throwEx();
    } else if (servers.size() == 1) {
      return servers;
    }
    var server = new Server();
    server.setName("server");
    return List.of(newServerCreator.create(server));
  }

  private static void throwEx() {
    throw new IllegalStateException("""
        Yous current application cant be run in mono server \
        because you have more thant one existing server""");
  }
}
