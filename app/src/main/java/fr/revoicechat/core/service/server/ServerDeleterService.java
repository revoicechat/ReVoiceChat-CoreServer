package fr.revoicechat.core.service.server;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerUser;
import fr.revoicechat.core.repository.RoomRepository;

@ApplicationScoped
@Transactional
public class ServerDeleterService {

  private final EntityManager entityManager;
  private final RoomRepository roomRepository;

  public ServerDeleterService(EntityManager entityManager, RoomRepository roomRepository) {
    this.entityManager = entityManager;
    this.roomRepository = roomRepository;
  }

  public void delete(final UUID id) {
    var server = entityManager.find(Server.class, id);
    var rooms = roomRepository.findByServerId(id);
    removeUserServer(server);
    rooms.forEach(entityManager::remove);
    entityManager.remove(server);
  }

  private void removeUserServer(final Server server) {
    entityManager.createQuery("""
                                  SELECT su
                                  FROM ServerUser su
                                  WHERE su.server = :server""", ServerUser.class)
                 .setParameter("server", server)
                 .getResultStream().forEach(entityManager::remove);
  }
}
