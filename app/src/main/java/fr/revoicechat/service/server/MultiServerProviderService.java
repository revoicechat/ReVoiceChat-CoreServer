package fr.revoicechat.service.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;

/**
 * {@link ServerProviderService} implementation for multiple-server mode.
 * <p>
 * In this mode, any number of {@link Server} instances may exist in the system.
 */
@Service
public class MultiServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(MultiServerProviderService.class);

  private final ServerRepository serverRepository;

  public MultiServerProviderService(final ServerRepository serverRepository) {this.serverRepository = serverRepository;}

  /** This implementation always allows usage. */
  @Override
  public void canBeUsed() {
    LOG.info("Multi server mode : can be used");
  }

  /** @return list of all servers, possibly empty */
  @Override
  public List<Server> getServers() {
    return serverRepository.findAll();
  }
}
