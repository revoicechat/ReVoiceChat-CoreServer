package fr.revoicechat.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.Room;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.service.MessageService;
import fr.revoicechat.service.RoomService;

@RestController
@RequestMapping("room/{id}")
public class RoomController {

  private final RoomService roomService;
  private final MessageService messageService;

  public RoomController(final RoomService roomService, final MessageService messageService) {
    this.roomService = roomService;
    this.messageService = messageService;
  }

  @GetMapping
  public Room read(@PathVariable("id") UUID roomId) {
    return roomService.read(roomId);
  }

  @PatchMapping
  public Room update(@PathVariable("id") UUID roomId,
                     @RequestBody RoomRepresentation representation) {
    return roomService.update(roomId, representation);
  }

  @DeleteMapping
  public UUID delete(@PathVariable("id") UUID roomId) {
    return roomService.delete(roomId);
  }

  @GetMapping("/message")
  public List<MessageRepresentation> messages(@PathVariable("id") UUID roomId) {
    return messageService.findAll(roomId);
  }

  @PutMapping("/message")
  public MessageRepresentation sendMessage(@PathVariable("id") UUID roomId,
                                           @RequestBody CreatedMessageRepresentation representation) {
    return messageService.create(roomId, representation);
  }
}
