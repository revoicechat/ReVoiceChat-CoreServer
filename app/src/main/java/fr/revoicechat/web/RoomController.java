package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.service.RoomService;

@RestController
@RequestMapping("server/{id}/room")
public class RoomController {

  private final RoomService roomService;

  public RoomController(final RoomService roomService) {
    this.roomService = roomService;
  }

  @GetMapping
  public List<Room> getRooms(@PathVariable final UUID id) {
    return roomService.findAll(id);
  }

  @PutMapping
  public Room createRoom(@PathVariable final UUID id, @RequestBody RoomRepresentation representation) {
    return roomService.create(id, representation.toEntity());
  }
}
