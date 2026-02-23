package fr.revoicechat.core.mapper;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.representation.RoomPresenceRepresentation;
import fr.revoicechat.core.service.user.UserService;
import fr.revoicechat.core.technicaldata.room.RoomPresence;
import fr.revoicechat.live.stream.service.StreamRetriever;
import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import fr.revoicechat.web.mapper.Mapper;
import fr.revoicechat.web.mapper.RepresentationMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoomPresenceMapper implements RepresentationMapper<RoomPresence, RoomPresenceRepresentation> {

  private final ConnectedUserRetriever connectedUserRetriever;
  private final StreamRetriever streamRetriever;
  private final UserService userService;

  public RoomPresenceMapper(ConnectedUserRetriever connectedUserRetriever, StreamRetriever streamRetriever, UserService userService) {
    this.connectedUserRetriever = connectedUserRetriever;
    this.streamRetriever = streamRetriever;
    this.userService = userService;
  }

  @Override
  public RoomPresenceRepresentation map(final RoomPresence presence) {
    List<User> allUser = userService.fetchUserForRoom(presence.room().getId());
    return new RoomPresenceRepresentation(
        presence.room().getId(),
        presence.room().getName(),
        Mapper.mapAll(allUser),
        map(presence.room().getId())
    );
  }

  List<ConnectedUserRepresentation> map(final UUID roomId) {
    return connectedUserRetriever.getConnectedUsers(roomId)
                                 .map(this::mapToRepresentation)
                                 .toList();
  }

  private ConnectedUserRepresentation mapToRepresentation(final UUID userId) {
    return new ConnectedUserRepresentation(Mapper.map(userService.getUser(userId)), streamRetriever.fetch(userId));
  }
}
