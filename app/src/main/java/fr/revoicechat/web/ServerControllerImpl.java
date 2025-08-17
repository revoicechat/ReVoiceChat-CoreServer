package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.Server;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.ServerService;
import fr.revoicechat.web.api.ServerController;

@RestController
public class ServerControllerImpl implements ServerController {

  private final ServerService serverService;
  private final RoomService roomService;

  public ServerControllerImpl(ServerService serverService, final RoomService roomService) {
    this.serverService = serverService;
    this.roomService = roomService;
  }

  @Override
  public List<Server> getServers() {
    return serverService.getAll();
  }

  @Override
  public Server getServer(@PathVariable UUID id) {
    return serverService.get(id);
  }

  @Override
  public Server createServer(@RequestBody ServerCreationRepresentation representation) {
    return serverService.create(representation.toEntity());
  }

  @Override
  public Server updateServer(@PathVariable UUID id,
                             @RequestBody ServerCreationRepresentation representation) {
    return serverService.update(id, representation.toEntity());
  }

  @Override
  public List<Room> getRooms(@PathVariable final UUID id) {
    return roomService.findAll(id);
  }

  @Override
  public Room createRoom(@PathVariable final UUID id, @RequestBody RoomRepresentation representation) {
    return roomService.create(id, representation);
  }
}
