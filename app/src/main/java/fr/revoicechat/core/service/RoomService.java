package fr.revoicechat.core.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.error.BadRequestException;
import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.nls.RoomErrorCode;
import fr.revoicechat.core.repository.RoomRepository;
import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomNotification;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;

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

  public RoomService(EntityManager entityManager, final RoomRepository repository, final ServerService serverService, RoomUserFinder roomUserFinder) {
    this.entityManager = entityManager;
    this.repository = repository;
    this.serverService = serverService;
    this.roomUserFinder = roomUserFinder;
  }

  /**
   * Retrieves all available rooms of a server.
   * @return a list of available rooms, possibly empty
   */
  public List<RoomRepresentation> findAll(final UUID id) {
    return repository.findByServerId(id).stream()
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
}
