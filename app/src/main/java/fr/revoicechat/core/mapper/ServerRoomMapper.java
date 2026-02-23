package fr.revoicechat.core.mapper;

import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.room.ServerRoom;
import fr.revoicechat.core.representation.RoomRepresentation;
import fr.revoicechat.core.service.room.RoomReadStatusService;
import fr.revoicechat.web.mapper.RepresentationMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerRoomMapper implements RepresentationMapper<ServerRoom, RoomRepresentation> {

  private final RoomReadStatusService roomReadStatusService;

  public ServerRoomMapper(final RoomReadStatusService roomReadStatusService) {
    this.roomReadStatusService = roomReadStatusService;
  }

  @Override
  public RoomRepresentation map(final ServerRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        getServerId(room),
        roomReadStatusService.getUnreadMessagesStatus(room)
    );
  }

  @Override
  public RoomRepresentation mapLight(final ServerRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        getServerId(room),
        null
    );
  }

  private static UUID getServerId(final ServerRoom room) {
    return Optional.ofNullable(room)
                   .map(ServerRoom::getServer)
                   .map(Server::getId)
                   .orElse(null);
  }
}
