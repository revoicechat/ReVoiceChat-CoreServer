package fr.revoicechat.core.repository.impl;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.RoomRepository;

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
}
