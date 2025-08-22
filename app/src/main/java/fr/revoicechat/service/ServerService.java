package fr.revoicechat.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.model.Server;
import fr.revoicechat.model.ServerUser;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.server.ServerProviderService;

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
  private static final Logger LOG = LoggerFactory.getLogger(ServerService.class);

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
   *
   * @return a list of available servers, possibly empty
   */
  public List<Server> getAll() {
    var servers = serverProviderService.getServers();
    LOG.info("servers LIST / {}", servers);
    return servers;
  }

  /**
   * Retrieves a server from the database by its unique identifier.
   *
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
   *
   * @param representation the server entity to persist
   * @return the persisted server entity with its generated ID
   */
  @Transactional
  public Server create(final ServerCreationRepresentation representation) {
    Server server = new Server();
    server.setId(UUID.randomUUID());
    server.setName(representation.name());
    var owner = userHolder.get();
    server.setOwner(userHolder.get());
    serverProviderService.create(server);
    ServerUser serverUser = new ServerUser();
    serverUser.setServer(server);
    serverUser.setUser(owner);
    entityManager.persist(serverUser);
    return server;
  }

  /**
   * Updates an existing server in the database.
   * <p>
   * The ID of the provided entity will be overridden with the given {@code id}
   * before persisting.
   *
   * @param id     the ID of the server to update
   * @param representation the updated server data
   * @return the updated and persisted server entity
   */
  @Transactional
  public Server update(final UUID id, final ServerCreationRepresentation representation) {
    var server = get(id);
    server.setName(representation.name());
    entityManager.persist(server);
    return server;
  }
}
