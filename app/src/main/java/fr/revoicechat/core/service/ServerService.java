package fr.revoicechat.core.service;

import static fr.revoicechat.notification.representation.NotificationActionType.MODIFY;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerUser;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.server.NewUserInServer;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.server.ServerUpdateNotification;
import fr.revoicechat.core.service.server.ServerEntityService;
import fr.revoicechat.core.service.server.ServerProviderService;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.risk.service.server.ServerRoleService;
import fr.revoicechat.security.UserHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Service responsible for managing {@link Server} entities.
 * <p>
 * This service acts as a bridge between the application layer and the persistence layer,
 * providing methods to:
 * <ul>
 *     <li>Retrieve a list of all servers (from an external provider)</li>
 *     <li>Retrieve a single server by its ID</li>
 *     <li>Create new servers</li>
 *     <li>Update existing servers</li>
 * </ul>
 * <p>
 * The list of servers returned by {@link #getAll()} comes from {@link ServerProviderService},
 * whereas other CRUD operations.
 */
@ApplicationScoped
public class ServerService {

  private final ServerEntityService serverEntityService;
  private final ServerProviderService serverProviderService;
  private final ServerRoleService serverRoleService;
  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final UserRepository userRepository;

  public ServerService(final ServerEntityService serverEntityService,
                       final ServerProviderService serverProviderService,
                       final ServerRoleService serverRoleService,
                       final UserHolder userHolder,
                       final EntityManager entityManager,
                       final UserRepository userRepository) {
    this.serverEntityService = serverEntityService;
    this.serverProviderService = serverProviderService;
    this.serverRoleService = serverRoleService;
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.userRepository = userRepository;
  }

  /**
   * Retrieves all available servers from the external provider.
   * <p>
   * This method does <b>not</b> query the database, but uses the
   * {@link ServerProviderService} to fetch the server list, then logs it.
   * @return a list of available servers, possibly empty
   */
  public List<ServerRepresentation> getAll() {
    return serverProviderService.getServers().stream()
                                .map(this::map)
                                .toList();
  }

  /**
   * Retrieves a server from the database by its unique identifier.
   * @param id the unique server ID
   * @return the server entity
   * @throws java.util.NoSuchElementException if no server with the given ID exists
   */
  public ServerRepresentation get(final UUID id) {
    return map(serverEntityService.getEntity(id));
  }

  /**
   * Creates and stores a new server in the database.
   * @param representation the server entity to persist
   * @return the persisted server entity with its generated ID
   */
  @Transactional
  public ServerRepresentation create(final ServerCreationRepresentation representation) {
    Server server = new Server();
    server.setName(representation.name());
    serverProviderService.create(server);
    ServerUser serverUser = new ServerUser();
    serverUser.setServer(server);
    serverUser.setUser(userHolder.get());
    entityManager.persist(serverUser);
    return map(server);
  }

  @Transactional
  public void joinDefaultServer(User user) {
    var servers = serverProviderService.getServers();
    if (servers.size() == 1) {
      var server = servers.getFirst();
      ServerUser serverUser = new ServerUser();
      serverUser.setServer(server);
      serverUser.setUser(user);
      entityManager.persist(serverUser);
      if (server.getOwner() == null) {
        server.setOwner(user);
        entityManager.persist(server);
      }
      serverRoleService.addUserToDefaultServerRole(user.getId(), server.getId());
      Notification.of(new NewUserInServer(server.getId(), user.getId())).sendTo(userRepository.findByServers(server.getId()));
    }
  }

  @Transactional
  public void delete(final UUID id) {
    serverProviderService.delete(id);
  }

  /**
   * Updates an existing server in the database.
   * <p>
   * The ID of the provided entity will be overridden with the given {@code id}
   * before persisting.
   * @param id             the ID of the server to update
   * @param representation the updated server data
   * @return the updated and persisted server entity
   */
  @Transactional
  public ServerRepresentation update(final UUID id, final ServerCreationRepresentation representation) {
    var server = serverEntityService.getEntity(id);
    server.setName(representation.name());
    entityManager.persist(server);
    var updatedServer = map(server);
    Notification.of(new ServerUpdateNotification(updatedServer, MODIFY)).sendTo(userRepository.findByServers(id));
    return updatedServer;
  }

  public ServerRepresentation map(final Server server) {
    return new ServerRepresentation(
        server.getId(),
        server.getName(),
        Optional.ofNullable(server.getOwner()).map(User::getId).orElse(null)
    );
  }
}
