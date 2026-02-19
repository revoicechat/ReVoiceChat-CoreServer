package fr.revoicechat.core.repository.impl.room;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.service.room.RoomAccessVerifier;
import fr.revoicechat.core.repository.RoomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class AccessibleRoomsResolver {

  private final EntityManager entityManager;
  private final RoomAccessVerifier roomAccessVerifier;
  private final RoomRepository roomRepository;

  public AccessibleRoomsResolver(EntityManager entityManager,
                                 RoomAccessVerifier roomAccessVerifier,
                         RoomRepository roomRepository) {
    this.entityManager = entityManager;
    this.roomAccessVerifier = roomAccessVerifier;
    this.roomRepository = roomRepository;
  }

  public Set<UUID> resolve(UUID currentUserId, UUID roomId) {
    return roomId != null
           ? resolveForSpecificRoom(currentUserId, roomId)
           : resolve(currentUserId);
  }

  public Set<UUID> resolveForSpecificRoom(final UUID currentUserId, final UUID roomId) {
    var room = entityManager.find(Room.class, roomId);
    return roomAccessVerifier.verify(currentUserId, room) ? Set.of(roomId) : Set.of();
  }

  public Set<UUID> resolve(UUID currentUserId) {
    return roomRepository.findRoomsByUserServers(currentUserId)
                         .filter(room -> roomAccessVerifier.verify(currentUserId, room))
                         .map(Room::getId)
                         .collect(Collectors.toSet());
  }
}
