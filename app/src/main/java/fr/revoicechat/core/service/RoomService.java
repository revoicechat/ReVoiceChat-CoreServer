package fr.revoicechat.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerItem;
import fr.revoicechat.core.model.server.ServerRoom;
import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.core.nls.RoomErrorCode;
import fr.revoicechat.core.repository.RoomRepository;
import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomNotification;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.core.risk.RoomRiskType;
import fr.revoicechat.web.error.BadRequestException;
import fr.revoicechat.web.error.ResourceNotFoundException;

/**
 * Service responsible for managing {@link Room} entities.
 * <p>
 * This service acts as a bridge between the application layer and the persistence layer,
 * providing methods to:
 * <ul>
 *     <li>Retrieve a list of all room</li>
 *     <li>Retrieve a single room by its ID</li>
 *     <li>Create new room</li>
 * </ul>
 * <p>
 */
@ApplicationScoped
public class RoomService {

  private final EntityManager entityManager;
  private final RoomRepository repository;
  private final ServerService serverService;
  private final RoomUserFinder roomUserFinder;
  private final RiskService riskService;

  public RoomService(EntityManager entityManager,
                     RoomRepository repository,
                     ServerService serverService,
                     RoomUserFinder roomUserFinder,
                     RiskService riskService) {
    this.entityManager = entityManager;
    this.repository = repository;
    this.serverService = serverService;
    this.roomUserFinder = roomUserFinder;
    this.riskService = riskService;
  }

  /**
   * Retrieves all available rooms of a server.
   * @return a list of available rooms, possibly empty
   */
  public List<RoomRepresentation> findAll(final UUID id) {
    return repository.findByServerId(id).stream().map(this::toReprsentation).toList();
  }

  /**
   * Retrieves all available rooms of a server.
   * @return a list of available rooms, possibly empty
   */
  public List<RoomRepresentation> findAllForCurrentUser(final UUID id) {
    return repository.findByServerId(id).stream()
                     .filter(room -> riskService.hasRisk(new RiskEntity(id, room.getId()), RoomRiskType.SERVER_ROOM_READ))
                     .map(this::toReprsentation)
                     .toList();
  }

  /**
   * Creates and stores a new room in the database.
   * @param id       the server id
   * @param creation the room entity to persist
   * @return the persisted room entity with its generated ID
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  @Transactional
  public RoomRepresentation create(final UUID id, final CreationRoomRepresentation creation) {
    var room = new Room();
    room.setId(UUID.randomUUID());
    room.setType(creation.type());
    room.setName(creation.name());
    var server = serverService.getEntity(id);
    room.setServer(server);
    entityManager.persist(room);
    var structure = new ServerStructure(new ArrayList<>());
    structure.items().addAll(server.getStructure().items());
    structure.items().add(new ServerRoom(room.getId()));
    var representation = toReprsentation(room);
    Notification.of(new RoomNotification(representation, NotificationActionType.ADD)).sendTo(roomUserFinder.find(room.getId()));
    return representation;
  }

  /**
   * Retrieves a room from the database by its unique identifier.
   * @param roomId the unique room ID
   * @return the room entity
   * @throws ResourceNotFoundException if no room with the given ID exists
   */
  public RoomRepresentation read(final UUID roomId) {
    return toReprsentation(getRoom(roomId));
  }

  /**
   * Update and stores a room in the database.
   * @param id       the room id
   * @param creation the room entity to persist
   * @return the persisted room entity
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  @Transactional
  public RoomRepresentation update(final UUID id, final CreationRoomRepresentation creation) {
    var room = getRoom(id);
    if (creation.type() != null && room.getType() != creation.type()) {
      throw new BadRequestException(RoomErrorCode.ROOM_TYPE_CANNOT_BE_CHANGED);
    }
    room.setName(creation.name());
    entityManager.persist(room);
    var representation = toReprsentation(room);
    Notification.of(new RoomNotification(representation, NotificationActionType.MODIFY)).sendTo(roomUserFinder.find(id));
    return representation;
  }

  /**
   * Creates and stores a new room in the database.
   * @param id the server id
   * @return the persisted room entity with its generated ID
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  @Transactional
  public UUID delete(final UUID id) {
    var room = getRoom(id);
    var server = room.getServer();
    server.setStructure(removeFromStructure(server.getStructure(), room));
    entityManager.persist(server);
    Optional.ofNullable(entityManager.find(Room.class, id)).ifPresent(entityManager::remove);
    Notification.of(new RoomNotification(new RoomRepresentation(id, null, null, null), NotificationActionType.REMOVE)).sendTo(roomUserFinder.find(id));
    return id;
  }

  public Room getRoom(final UUID roomId) {
    return Optional.ofNullable(entityManager.find(Room.class, roomId)).orElseThrow(() -> new ResourceNotFoundException(Room.class, roomId));
  }

  private RoomRepresentation toReprsentation(final Room room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        room.getServer().getId()
    );
  }

  private ServerStructure removeFromStructure(ServerStructure structure, Room room) {
    return new ServerStructure(
        structure.items().stream()
                 .map(i -> removeFromStructure(i, room.getId()))
                 .filter(Objects::nonNull)
                 .toList());
  }

  private ServerItem removeFromStructure(ServerItem item, UUID roomId) {
    return switch (item) {
      case ServerRoom room when room.id().equals(roomId) -> null;
      case ServerCategory(String name, List<ServerItem> items) -> new ServerCategory(name, items.stream()
                                                                                                .map(i -> removeFromStructure(i, roomId))
                                                                                                .filter(Objects::nonNull)
                                                                                                .toList());
      case null, default -> item;
    };
  }
}
