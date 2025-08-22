package fr.revoicechat.service.server;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.Server;
import fr.revoicechat.model.User;
import fr.revoicechat.repository.ServerRepository;
import fr.revoicechat.repository.UserRepository;

/**
 * {@link ServerProviderService} implementation for multiple-server mode.
 * <p>
 * In this mode, any number of {@link Server} instances may exist in the system.
 */
@Service
public class MultiServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(MultiServerProviderService.class);

  private final ServerRepository serverRepository;
  private final UserRepository userRepository;
  private final NewServerCreator newServerCreator;

  public MultiServerProviderService(final ServerRepository serverRepository, final UserRepository userRepository, final NewServerCreator newServerCreator) {this.serverRepository = serverRepository;
    this.userRepository = userRepository;
    this.newServerCreator = newServerCreator;
  }

  /** This implementation always allows usage. */
  @Override
  public void canBeUsed() {
    LOG.info("Multi server mode : enabled");
  }

  /** @return list of all servers, possibly empty */
  @Override
  public List<Server> getServers() {
    return serverRepository.findAll();
  }

  @Override
  public Stream<User> getUsers(UUID id) {
    return userRepository.findByServers(id);
  }

  @Override
  public Server create(final Server entity) {
    LOG.info("Creation of server {}", entity.getName());
    return newServerCreator.create(entity);
  }


}
