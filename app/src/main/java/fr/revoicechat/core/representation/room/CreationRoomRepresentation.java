package fr.revoicechat.core.representation.room;

import fr.revoicechat.core.model.room.RoomType;

public record CreationRoomRepresentation(
    String name,
    RoomType type
) {}
