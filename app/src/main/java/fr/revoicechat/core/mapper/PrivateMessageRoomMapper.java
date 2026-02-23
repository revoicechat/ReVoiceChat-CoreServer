package fr.revoicechat.core.mapper;

import fr.revoicechat.core.model.room.PrivateMessageRoom;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.room.RoomReadStatusService;
import fr.revoicechat.web.mapper.RepresentationMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrivateMessageRoomMapper implements RepresentationMapper<PrivateMessageRoom, RoomRepresentation> {

  private final RoomReadStatusService roomReadStatusService;

  public PrivateMessageRoomMapper(final RoomReadStatusService roomReadStatusService) {
    this.roomReadStatusService = roomReadStatusService;
  }

  @Override
  public RoomRepresentation map(final PrivateMessageRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        null,
        roomReadStatusService.getUnreadMessagesStatus(room)
    );
  }

  @Override
  public RoomRepresentation mapLight(final PrivateMessageRoom room) {
    return new RoomRepresentation(
        room.getId(),
        room.getName(),
        room.getType(),
        null,
        null
    );
  }
}
