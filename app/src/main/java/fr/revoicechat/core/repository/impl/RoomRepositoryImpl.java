package fr.revoicechat.core.repository.impl;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.RoomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class RoomRepositoryImpl implements RoomRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Room> findByServerId(UUID serverId) {
    return entityManager
        .createQuery("SELECT r FROM Room r where r.server.id = :serverId", Room.class)
        .setParameter("serverId", serverId)
        .getResultList();
  }

  @Override
  public List<UUID> findIdThatAreNotInRoom(UUID serverId, List<UUID> ids) {
    var idsIn = entityManager.createQuery("""
                                     SELECT r.id
                                     FROM Room r
                                     WHERE r.id IN :ids
                                     and r.server.id = :serverId
                                 """, UUID.class)
                             .setParameter("ids", ids)
                             .setParameter("serverId", serverId)
                             .getResultList();
    return ids.stream().filter(not(idsIn::contains)).toList();
  }

  @Override
  public UUID getServerId(final UUID room) {
    return entityManager
        .createQuery("SELECT r.server.id FROM Room r where r.id = :room", UUID.class)
        .setParameter("room", room)
        .getResultStream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public Stream<Room> findRoomsByUserServers(final UUID userId) {
    return entityManager.createQuery("""
                                SELECT r
                                FROM Room r
                                JOIN ServerUser su on su.server = r.server
                                WHERE su.user.id = :id
                            """, Room.class)
                        .setParameter("id", userId)
                        .getResultStream();
  }
}
