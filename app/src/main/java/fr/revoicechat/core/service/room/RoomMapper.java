package fr.revoicechat.core.service.room;

import fr.revoicechat.core.model.room.ServerRoom;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoomMapper {

  private final RoomReadStatusService roomReadStatusService;

  public RoomMapper(final RoomReadStatusService roomReadStatusService) {
    this.roomReadStatusService = roomReadStatusService;
  }

  public RoomRepresentation map(final ServerRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        room.getServer().getId(),
        roomReadStatusService.getUnreadMessagesStatus(room)
    );
  }

  public RoomRepresentation mapLight(final ServerRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        room.getServer().getId(),
        null
    );
  }
}
