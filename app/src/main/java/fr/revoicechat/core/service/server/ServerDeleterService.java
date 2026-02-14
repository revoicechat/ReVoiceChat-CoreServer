package fr.revoicechat.core.service.server;

import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.repository.RoomRepository;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.risk.service.server.ServerRolesDeleterService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class ServerDeleterService {

  private final EntityManager entityManager;
  private final ServerRepository serverRepository;
  private final RoomRepository roomRepository;
  private final ServerRolesDeleterService serverRolesDeleterService;

  public ServerDeleterService(EntityManager entityManager,
                              ServerRepository serverRepository,
                              RoomRepository roomRepository,
                              ServerRolesDeleterService serverRolesDeleterService) {
    this.entityManager = entityManager;
    this.serverRepository = serverRepository;
    this.roomRepository = roomRepository;
    this.serverRolesDeleterService = serverRolesDeleterService;
  }

  public void delete(final UUID id) {
    var server = entityManager.find(Server.class, id);
    removeUserServer(server);
    removeRoom(server);
    serverRolesDeleterService.delete(id);
    entityManager.remove(server);
  }

  private void removeRoom(final Server server) {
    roomRepository.findByServerId(server.getId()).forEach(entityManager::remove);
  }

  private void removeUserServer(final Server server) {
    serverRepository.getServerUser(server).forEach(entityManager::remove);
  }
}
