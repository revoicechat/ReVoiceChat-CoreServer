package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.Server;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.ServerService;
import fr.revoicechat.service.UserService;
import fr.revoicechat.web.api.ServerController;

@RestController
public class ServerControllerImpl implements ServerController {

  private final ServerService serverService;
  private final RoomService roomService;
  private final UserService userService;

  public ServerControllerImpl(ServerService serverService, final RoomService roomService, final UserService userService) {
    this.serverService = serverService;
    this.roomService = roomService;
    this.userService = userService;
  }

  @Override
  public List<Server> getServers() {
    return serverService.getAll();
  }

  @Override
  public Server getServer(UUID id) {
    return serverService.get(id);
  }

  @Override
  public Server createServer(ServerCreationRepresentation representation) {
    return serverService.create(representation);
  }

  @Override
  public Server updateServer(UUID id, ServerCreationRepresentation representation) {
    return serverService.update(id, representation.toEntity());
  }

  @Override
  public List<Room> getRooms(UUID id) {
    return roomService.findAll(id);
  }

  @Override
  public Room createRoom(UUID id, RoomRepresentation representation) {
    return roomService.create(id, representation);
  }

  @Override
  public List<UserRepresentation> fetchUsers(final UUID id) {
    return userService.fetchUserForServer(id);
  }
}
