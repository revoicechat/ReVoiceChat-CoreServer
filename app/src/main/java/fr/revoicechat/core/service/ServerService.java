package fr.revoicechat.core.service;

import static fr.revoicechat.notification.representation.NotificationActionType.MODIFY;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerType;
import fr.revoicechat.core.model.ServerUser;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.server.ServerUpdateNotification;
import fr.revoicechat.core.risk.ServerRiskType;
import fr.revoicechat.core.service.server.NewServerCreator;
import fr.revoicechat.core.service.server.ServerEntityService;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.technicaldata.RiskEntity;
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
 */
@ApplicationScoped
public class ServerService {

  private final ServerEntityService serverEntityService;
  private final ServerRepository serverRepository;
  private final NewServerCreator newServerCreator;
  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final RiskService riskService;

  public ServerService(final ServerEntityService serverEntityService,
                       final ServerRepository serverRepository,
                       final NewServerCreator newServerCreator,
                       final UserHolder userHolder,
                       final EntityManager entityManager,
                       final UserRepository userRepository,
                       final RiskService riskService) {
    this.serverEntityService = serverEntityService;
    this.serverRepository = serverRepository;
    this.newServerCreator = newServerCreator;
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.userRepository = userRepository;
    this.riskService = riskService;
  }

  /** @return a list of available servers, possibly empty */
  public List<ServerRepresentation> getAll() {
    return serverRepository.findAll().stream().map(this::map).toList();
  }

  /**
   * Retrieves a server from the database by its unique identifier.
   *
   * @param id the unique server ID
   * @return the server entity
   * @throws java.util.NoSuchElementException if no server with the given ID exists
   */
  public ServerRepresentation get(final UUID id) {
    return map(serverEntityService.getEntity(id));
  }

  /**
   * Creates and stores a new server in the database.
   *
   * @param representation the server entity to persist
   * @return the persisted server entity with its generated ID
   */
  @Transactional
  public ServerRepresentation create(final ServerCreationRepresentation representation) {
    Server server = new Server();
    server.setName(representation.name());
    server.setType(Optional.ofNullable(representation.serverType()).orElse(ServerType.PUBLIC));
    newServerCreator.create(server);
    ServerUser serverUser = new ServerUser();
    serverUser.setServer(server);
    serverUser.setUser(userHolder.get());
    entityManager.persist(serverUser);
    return map(server);
  }

  /**
   * Updates an existing server in the database.
   * <p>
   * The ID of the provided entity will be overridden with the given {@code id}
   * before persisting.
   *
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
        Optional.ofNullable(server.getOwner()).map(User::getId).orElse(null),
        riskService.hasRisk(new RiskEntity(server.getId(), null), ServerRiskType.SERVER_UPDATE)
    );
  }
}
