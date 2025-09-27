package fr.revoicechat.core.service.server;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.security.UserHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerRoom;
import fr.revoicechat.core.model.server.ServerStructure;

@ApplicationScoped
public class NewServerCreator {

  private final EntityManager entityManager;
  private final UserHolder holder;

  public NewServerCreator(EntityManager entityManager, final UserHolder holder) {
    this.entityManager = entityManager;
    this.holder = holder;
  }

  @Transactional
  public Server create(Server server) {
    server.setId(UUID.randomUUID());
    server.setOwner(holder.get());
    entityManager.persist(server);
    var general = createRoom(server, "General",  RoomType.TEXT);
    var random = createRoom(server, "Random",   RoomType.TEXT);
    var vocal = createRoom(server, "Vocal", RoomType.VOICE);
    server.setStructure(new ServerStructure(List.of(
        new ServerCategory("text", List.of(
            new ServerRoom(general.getId()),
            new ServerRoom(random.getId())
        )),
        new ServerCategory("vocal", List.of(new ServerRoom(vocal.getId())))
    )));
    entityManager.persist(server);
    return server;
  }

  private Room createRoom(final Server server, final String name, RoomType type) {
    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setName(name);
    room.setServer(server);
    room.setType(type);
    entityManager.persist(room);
    return room;
  }
}
