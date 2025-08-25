package fr.revoicechat.service.server;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.RoomType;
import fr.revoicechat.model.Server;

@ApplicationScoped
public class NewServerCreator {

  private final EntityManager entityManager;

  public NewServerCreator(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Transactional
  public Server create(Server server) {
    server.setId(UUID.randomUUID());
    entityManager.persist(server);
    createRoom(server, "📝 General",  RoomType.TEXT);
    createRoom(server, "📝 Random",   RoomType.TEXT);
    createRoom(server, "🔊 Vocal", RoomType.WEBRTC);
    return server;
  }

  private void createRoom(final Server server, final String name, RoomType type) {
    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setName(name);
    room.setServer(server);
    room.setType(type);
    entityManager.persist(room);
  }
}
