package fr.revoicechat.core.representation.room;

import java.util.UUID;

import fr.revoicechat.core.model.room.RoomType;
import fr.revoicechat.core.representation.message.UnreadMessageStatus;

public record RoomRepresentation(
    UUID id,
    String name,
    RoomType type,
    UUID serverId,
    UnreadMessageStatus unreadMessages
) {}
