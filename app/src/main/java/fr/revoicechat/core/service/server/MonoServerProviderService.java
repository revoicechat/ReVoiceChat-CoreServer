package fr.revoicechat.core.service.server;

import static fr.revoicechat.core.config.SeverAppMode.MONO_SERVER;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.nls.ServerErrorCode;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.web.error.BadRequestException;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * {@link ServerProviderService} implementation for single-server mode.
 * <p>
 * In this mode, only one {@link Server} may exist in the system.
 * If no server is found, a new one is automatically created.
 */
@ApplicationScoped
@IfBuildProperty(name = "revoicechat.global.sever-mode", stringValue = MONO_SERVER)
public class MonoServerProviderService implements ServerProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(MonoServerProviderService.class);

  static final String ERROR_MESSAGE = """
      Your current application can't be run in mono server \
      because you have more than one existing server""";

  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;
  private final UserRepository userRepository;

  public MonoServerProviderService(final ServerRepository serverRepository, final NewServerCreator newServerCreator, final UserRepository userRepository) {
    this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
    this.userRepository = userRepository;
  }

  /**
   * Ensures that the application is in a valid single-server state.
   * <p>
   * This method throws an exception if more than one server exists.
   * @throws IllegalStateException if more than one server exists
   */
  @PostConstruct
  public void init() {
    if (serverRepository.count() > 1) {
      LOG.error("Mono server mode : error");
      throw new IllegalStateException(ERROR_MESSAGE);
    }
    LOG.info("Mono server mode : available");
  }

  /**
   * Returns the single server available in the system, creating one if none exists.
   * <p>
   * Throws an exception if more than one server is present.
   * @return a list containing the single server
   * @throws IllegalStateException if more than one server exists
   */
  @Override
  @Transactional
  public List<Server> getServers() {
    var servers = serverRepository.findAll();
    if (servers.size() > 1) {
      throw new IllegalStateException(ERROR_MESSAGE);
    } else if (servers.size() == 1) {
      return servers;
    }
    var server = new Server();
    server.setName("Server");
    return List.of(newServerCreator.create(server));
  }

  @Override
  public Stream<User> getUsers(UUID id) {
    return userRepository.findAll().stream();
  }

  @Override
  public void create(final Server entity) {
    LOG.error("{}", ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION);
    throw new BadRequestException(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION);
  }

  @Override
  public void delete(final UUID id) {
    LOG.error("{}", ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_DELETION);
    throw new BadRequestException(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_DELETION);
  }
}
