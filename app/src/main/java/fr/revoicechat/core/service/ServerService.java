package fr.revoicechat.core.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerUser;
import fr.revoicechat.core.model.UserType;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.service.server.ServerProviderService;
import io.quarkus.security.UnauthorizedException;

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
  private final ServerProviderService serverProviderService;
  private final UserHolder userHolder;
  private final EntityManager entityManager;

  public ServerService(ServerProviderService serverProviderService,
                       UserHolder userHolder,
                       EntityManager entityManager) {
    this.serverProviderService = serverProviderService;
    this.userHolder = userHolder;
    this.entityManager = entityManager;
  }

  /**
   * Retrieves all available servers from the external provider.
   * <p>
   * This method does <b>not</b> query the database, but uses the
   * {@link ServerProviderService} to fetch the server list, then logs it.
   * @return a list of available servers, possibly empty
   */
  public List<Server> getAll() {
    return serverProviderService.getServers();
  }

  /**
   * Retrieves a server from the database by its unique identifier.
   * @param id the unique server ID
   * @return the server entity
   * @throws java.util.NoSuchElementException if no server with the given ID exists
   */
  public Server get(final UUID id) {
    return Optional.ofNullable(entityManager.find(Server.class, id))
                   .orElseThrow(() -> new ResourceNotFoundException(Server.class, id));
  }

  /**
   * Creates and stores a new server in the database.
   * @param representation the server entity to persist
   * @return the persisted server entity with its generated ID
   */
  @Transactional
  public Server create(final ServerCreationRepresentation representation) {
    Server server = new Server();
    server.setId(UUID.randomUUID());
    server.setName(representation.name());
    var owner = userHolder.get();
    server.setOwner(owner);
    serverProviderService.create(server);
    ServerUser serverUser = new ServerUser();
    serverUser.setServer(server);
    serverUser.setUser(owner);
    entityManager.persist(serverUser);
    return server;
  }

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
  public Server update(final UUID id, final ServerCreationRepresentation representation) {
    var server = get(id);
    if (cannotBeUpdate(server)) {
      throw new UnauthorizedException("user is not allowed to update this server");
    }
    server.setName(representation.name());
    entityManager.persist(server);
    return server;
  }

  private boolean cannotBeUpdate(final Server server) {
    var user = userHolder.get();
    return !(user != null && (user.getType().equals(UserType.ADMIN) || server.getOwner().equals(user)));
  }
}
