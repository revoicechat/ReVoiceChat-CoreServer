package fr.revoicechat.core.representation.server;

import java.util.UUID;

public record ServerRepresentation(
    UUID id,
    String name,
    UUID owner
) {}
