package fr.revoicechat.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.model.Room;
import fr.revoicechat.nls.RoomErrorCode;
import fr.revoicechat.repository.RoomRepository;
import fr.revoicechat.representation.room.RoomRepresentation;

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
@Service
public class RoomService {

  private final EntityManager entityManager;
  private final RoomRepository repository;
  private final ServerService serverService;

  public RoomService(EntityManager entityManager, final RoomRepository repository, final ServerService serverService) {
    this.entityManager = entityManager;
    this.repository = repository;
    this.serverService = serverService;
  }

  /**
   * Retrieves all available rooms of a server.
   *
   * @return a list of available rooms, possibly empty
   */
  public List<Room> findAll(final UUID id) {
    return repository.findByServerId(id);
  }

  /**
   * Creates and stores a new room in the database.
   *
   * @param id the server id
   * @param representation the room entity to persist
   * @return the persisted room entity with its generated ID
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  @Transactional
  public Room create(final UUID id, final RoomRepresentation representation) {
    var room = new Room();
    room.setId(UUID.randomUUID());
    room.setType(representation.type());
    room.setName(representation.name());
    var server = serverService.get(id);
    room.setServer(server);
    entityManager.persist(room);
    return room;
  }

  /**
   * Retrieves a room from the database by its unique identifier.
   *
   * @param roomId the unique room ID
   * @return the room entity
   * @throws ResourceNotFoundException if no room with the given ID exists
   */
  public Room read(final UUID roomId) {
    return getRoom(roomId);
  }

  /**
   * Update and stores a room in the database.
   *
   * @param id the room id
   * @param representation the room entity to persist
   * @return the persisted room entity
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  @Transactional
  public Room update(final UUID id, final RoomRepresentation representation) {
    var room = getRoom(id);
    if (representation.type() != null && room.getType() != representation.type()) {
      throw new BadRequestException(RoomErrorCode.ROOM_TYPE_CANNOT_BE_CHANGED);
    }
    room.setName(representation.name());
    entityManager.persist(room);
    return room;
  }

  /**
   * Creates and stores a new room in the database.
   *
   * @param id the server id
   * @return the persisted room entity with its generated ID
   * @throws ResourceNotFoundException if no server with the given ID exists
   */
  public UUID delete(final UUID id) {
    Optional.ofNullable(entityManager.find(Room.class, id))
                .ifPresent(entityManager::remove);
    return id;
  }

  private Room getRoom(final UUID roomId) {
    return Optional.ofNullable(entityManager.find(Room.class, roomId))
                   .orElseThrow(() -> new ResourceNotFoundException(Room.class, roomId));
  }
}
