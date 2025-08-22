package fr.revoicechat.repository.impl;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import fr.revoicechat.model.Room;
import fr.revoicechat.repository.RoomRepository;

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
}
