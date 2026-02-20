package fr.revoicechat.core.service.room;

import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.room.Room;
import fr.revoicechat.core.model.room.ServerRoom;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoomMapper {

  private final RoomReadStatusService roomReadStatusService;

  public RoomMapper(final RoomReadStatusService roomReadStatusService) {
    this.roomReadStatusService = roomReadStatusService;
  }

  public RoomRepresentation map(final Room room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        getServerId(room),
        roomReadStatusService.getUnreadMessagesStatus(room)
    );
  }

  public RoomRepresentation mapLight(final Room room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        getServerId(room),
        null
    );
  }

  private static UUID getServerId(final Room room) {
    return Optional.ofNullable(room)
                   .filter(ServerRoom.class::isInstance)
                   .map(ServerRoom.class::cast)
                   .map(ServerRoom::getServer)
                   .map(Server::getId)
                   .orElse(null);
  }
}
