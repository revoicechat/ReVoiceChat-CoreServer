package fr.revoicechat.core.web;

import java.util.List;
import java.util.UUID;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.core.web.api.ServerController;

@RolesAllowed("USER") // only authenticated users
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
  public List<ServerRepresentation> getServers() {
    return serverService.getAll();
  }

  @Override
  public ServerRepresentation getServer(UUID id) {
    return serverService.get(id);
  }

  @Override
  public ServerRepresentation createServer(ServerCreationRepresentation representation) {
    return serverService.create(representation);
  }

  @Override
  public ServerRepresentation updateServer(UUID id, ServerCreationRepresentation representation) {
    return serverService.update(id, representation);
  }

  @Override
  public void deleteServer(final UUID id) {
    serverService.delete(id);
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
