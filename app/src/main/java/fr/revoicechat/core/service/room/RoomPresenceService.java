package fr.revoicechat.core.service.room;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.room.ConnectedUserRepresentation;
import fr.revoicechat.core.representation.room.RoomPresence;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.live.stream.service.StreamRetriever;
import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoomPresenceService {

  private final RoomService roomService;
  private final UserService userService;
  private final ConnectedUserRetriever connectedUserRetriever;
  private final StreamRetriever streamRetriever;

  public RoomPresenceService(RoomService roomService,
                             UserService userService,
                             ConnectedUserRetriever connectedUserRetriever,
                             StreamRetriever streamRetriever) {
    this.roomService = roomService;
    this.userService = userService;
    this.connectedUserRetriever = connectedUserRetriever;
    this.streamRetriever = streamRetriever;
  }

  public RoomPresence get(final UUID roomId) {
    var room = roomService.getRoom(roomId);
    List<UserRepresentation> allUser = userService.fetchUserForRoom(roomId);
    if (room.getType().isTextual()) {
      return new RoomPresence(roomId, room.getName(), allUser, List.of());
    }
    return new RoomPresence(roomId, room.getName(), allUser, getConnectedUser(roomId));
  }

  private List<ConnectedUserRepresentation> getConnectedUser(final UUID roomId) {
    return connectedUserRetriever.getConnectedUsers(roomId)
                                 .map(this::mapToRepresentation)
                                 .toList();
  }

  private ConnectedUserRepresentation mapToRepresentation(final UUID userId) {
    return new ConnectedUserRepresentation(userService.get(userId), streamRetriever.fetch(userId));
  }
}
