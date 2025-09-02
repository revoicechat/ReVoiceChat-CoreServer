package fr.revoicechat.core.service.server;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.core.repository.UserRepository;
import io.quarkus.arc.properties.IfBuildProperty;

/**
 * {@link ServerProviderService} implementation for multiple-server mode.
 * <p>
 * In this mode, any number of {@link Server} instances may exist in the system.
 */
@ApplicationScoped
@IfBuildProperty(name = "revoicechat.global.sever-mode", stringValue = "MULTI_SERVER")
public class MultiServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(MultiServerProviderService.class);

  private final ServerRepository serverRepository;
  private final UserRepository userRepository;
  private final NewServerCreator newServerCreator;
  private final ServerDeleterService serverDeleterService;

  public MultiServerProviderService(ServerRepository serverRepository,
                                    UserRepository userRepository,
                                    NewServerCreator newServerCreator,
                                    ServerDeleterService serverDeleterService) {
    this.serverRepository = serverRepository;
    this.userRepository = userRepository;
    this.newServerCreator = newServerCreator;
    this.serverDeleterService = serverDeleterService;
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

  @Override
  public void delete(final UUID id) {
    serverDeleterService.delete(id);
  }
}
