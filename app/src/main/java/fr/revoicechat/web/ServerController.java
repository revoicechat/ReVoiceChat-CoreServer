package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.Server;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.ServerService;

@RestController
@RequestMapping("server")
public class ServerController {

  private final ServerService serverService;
  private final RoomService roomService;

  public ServerController(ServerService serverService, final RoomService roomService) {
    this.serverService = serverService;
    this.roomService = roomService;
  }

  @GetMapping
  public List<Server> getServers() {
    return serverService.getAll();
  }

  @GetMapping("/{id}")
  public Server getServer(@PathVariable UUID id) {
    return serverService.get(id);
  }

  @PutMapping
  public Server createServer(@RequestBody ServerCreationRepresentation representation) {
    return serverService.create(representation.toEntity());
  }

  @PostMapping("/{id}")
  public Server updateServer(@PathVariable UUID id,
                             @RequestBody ServerCreationRepresentation representation) {
    return serverService.update(id, representation.toEntity());
  }

  @GetMapping("/{id}/room")
  public List<Room> getRooms(@PathVariable final UUID id) {
    return roomService.findAll(id);
  }

  @PutMapping("/{id}/room")
  public Room createRoom(@PathVariable final UUID id, @RequestBody RoomRepresentation representation) {
    return roomService.create(id, representation);
  }
}
