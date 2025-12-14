package fr.revoicechat.core.service.server;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerRoom;
import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.service.server.ServerRoleDefaultCreator;
import fr.revoicechat.security.UserHolder;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Unremovable
@ApplicationScoped
public class NewServerCreator {

  private final EntityManager entityManager;
  private final UserHolder holder;
  private final ServerRoleDefaultCreator serverRoleCreator;

  public NewServerCreator(EntityManager entityManager, UserHolder holder, ServerRoleDefaultCreator serverRoleCreator) {
    this.entityManager = entityManager;
    this.holder = holder;
    this.serverRoleCreator = serverRoleCreator;
  }

  @Transactional
  public Server create(Server server) {
    server.setId(UUID.randomUUID());
    server.setOwner(holder.getOrNull());
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
    serverRoleCreator.createDefault(server.getId());
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
