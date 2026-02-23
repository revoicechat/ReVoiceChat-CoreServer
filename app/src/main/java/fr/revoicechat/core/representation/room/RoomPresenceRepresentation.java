package fr.revoicechat.core.representation.room;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.user.UserRepresentation;

/**
 * @param allUser Represent all the users that have access to a room.
 * @param connectedUser Represent all the users that are currently connected to a room.
 *                      If the room is a text room, this list is empty
 */
public record RoomPresenceRepresentation(
    UUID id,
    String name,
    List<UserRepresentation> allUser,
    List<ConnectedUserRepresentation> connectedUser
) {
}
