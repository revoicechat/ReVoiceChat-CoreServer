package fr.revoicechat.core.service;

import static fr.revoicechat.core.nls.ServerErrorCode.SERVER_STRUCTURE_WITH_ROOM_THAT_DOES_NOT_EXISTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.risk.service.server.ServerFinder;
import fr.revoicechat.web.error.BadRequestException;
import fr.revoicechat.web.error.ResourceNotFoundException;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerUser;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.model.UserType;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerItem;
import fr.revoicechat.core.model.server.ServerRoom;
import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.core.repository.RoomRepository;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.server.ServerUpdateNotification;
import fr.revoicechat.core.service.server.ServerProviderService;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.security.UserHolder;
import io.quarkus.security.UnauthorizedException;
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
public class ServerService implements ServerFinder {
  private final ServerProviderService serverProviderService;
  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final RoomRepository roomRepository;
  private final RoomService roomService;
  private final UserRepository userRepository;

  public ServerService(final ServerProviderService serverProviderService,
                       final UserHolder userHolder,
                       final EntityManager entityManager,
                       final RoomRepository roomRepository,
                       final RoomService roomService,
                       final UserRepository userRepository) {
    this.serverProviderService = serverProviderService;
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.roomRepository = roomRepository;
    this.roomService = roomService;
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
    return map(getEntity(id));
  }

  @Override
  public void existsOrThrow(final UUID id) {
    getEntity(id);
  }

  public Server getEntity(final UUID id) {
    return Optional.ofNullable(entityManager.find(Server.class, id))
                   .orElseThrow(() -> new ResourceNotFoundException(Server.class, id));
  }

  /**
   * Creates and stores a new server in the database.
   * @param representation the server entity to persist
   * @return the persisted server entity with its generated ID
   */
  @Transactional
  public ServerRepresentation create(final ServerCreationRepresentation representation) {
    Server server = new Server();
    server.setId(UUID.randomUUID());
    server.setName(representation.name());
    serverProviderService.create(server);
    ServerUser serverUser = new ServerUser();
    serverUser.setServer(server);
    serverUser.setUser(userHolder.get());
    entityManager.persist(serverUser);
    return map(server);
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
    var server = getEntity(id);
    if (cannotBeUpdate(server)) {
      throw new UnauthorizedException("user is not allowed to update this server");
    }
    server.setName(representation.name());
    entityManager.persist(server);
    return map(server);
  }

  private boolean cannotBeUpdate(final Server server) {
    User user = userHolder.get();
    return !user.getType().equals(UserType.ADMIN) && !server.getOwner().equals(user);
  }

  private ServerRepresentation map(final Server server) {
    return new ServerRepresentation(
        server.getId(),
        server.getName(),
        Optional.ofNullable(server.getOwner()).map(User::getId).orElse(null)
    );
  }

  public ServerStructure getStructure(final UUID id) {
    return Optional.ofNullable(getEntity(id).getStructure())
                   .orElseGet(() -> new ServerStructure(new ArrayList<>(roomService.findAll(id).stream()
                                                                                   .map(RoomRepresentation::id)
                                                                                   .map(ServerRoom::new)
                                                                                   .toList())));
  }

  @Transactional
  public ServerStructure updateStructure(final UUID id, final ServerStructure structure) {
    List<UUID> roomId = structure == null ? List.of() : flatStructure(structure.items(), new ArrayList<>());
    if (!roomId.isEmpty() && !roomRepository.findIdThatAreNotInRoom(id, roomId).isEmpty()) {
      throw new BadRequestException(SERVER_STRUCTURE_WITH_ROOM_THAT_DOES_NOT_EXISTS);
    }
    var server = getEntity(id);
    server.setStructure(structure);
    entityManager.persist(server);
    var newStructure = getStructure(id);
    Notification.of(new ServerUpdateNotification(map(server), NotificationActionType.MODIFY)).sendTo(userRepository.findByServers(id));
    return newStructure;
  }

  private List<UUID> flatStructure(final List<ServerItem> structure, List<UUID> ids) {
    structure.forEach(item -> {
      switch (item) {
        case ServerRoom(UUID id) -> ids.add(id);
        case ServerCategory category -> flatStructure(category.items(), ids);
      }
    });
    return ids;
  }
}
