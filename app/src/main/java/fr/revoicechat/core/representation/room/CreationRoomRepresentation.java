package fr.revoicechat.core.representation.room;

import fr.revoicechat.core.model.RoomType;

public record CreationRoomRepresentation(
    String name,
    RoomType type
) {}
