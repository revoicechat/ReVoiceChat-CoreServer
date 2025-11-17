package fr.revoicechat.core.service.room;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.representation.room.RoomPresence;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.UserService;

@ApplicationScoped
public class RoomPresenceService {

  private final RoomService roomService;
  private final UserService userService;
  private final ConnectedUserRetriever connectedUserRetriever;

  public RoomPresenceService(RoomService roomService, UserService userService, ConnectedUserRetriever connectedUserRetriever) {
    this.roomService = roomService;
    this.userService = userService;
    this.connectedUserRetriever = connectedUserRetriever;
  }

  public RoomPresence get(final UUID id) {
    var room = roomService.getRoom(id);
    List<UserRepresentation> allUser = userService.fetchUserForRoom(id);
    if (room.getType().equals(RoomType.TEXT)) {
      return new RoomPresence(id, room.getName(), allUser, List.of());
    }
    List<UserRepresentation> connectedUser = connectedUserRetriever.getConnectedUsers(id).map(userService::get).toList();
    return new RoomPresence(id, room.getName(), allUser, connectedUser);
  }
}
