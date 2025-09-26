package fr.revoicechat.core.representation.user;

import java.time.OffsetDateTime;
import java.util.UUID;

import fr.revoicechat.core.model.ActiveStatus;
import fr.revoicechat.core.model.UserType;

public record UserRepresentation(
    UUID id,
    String displayName,
    String login,
    OffsetDateTime createdDate,
    ActiveStatus status,
    UserType type
) {}
