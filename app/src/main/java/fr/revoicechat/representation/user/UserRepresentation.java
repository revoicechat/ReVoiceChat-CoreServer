package fr.revoicechat.representation.user;

import java.time.OffsetDateTime;
import java.util.UUID;

import fr.revoicechat.model.ActiveStatus;

public record UserRepresentation(
    UUID id,
    String displayName,
    String login,
    OffsetDateTime createdDate,
    ActiveStatus status
) {}
