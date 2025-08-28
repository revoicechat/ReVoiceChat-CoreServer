package fr.revoicechat.core.representation.room;

import fr.revoicechat.core.model.RoomType;

public record RoomRepresentation(
    String name,
    RoomType type
) {}
