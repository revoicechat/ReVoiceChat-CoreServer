package fr.revoicechat.representation.user;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.revoicechat.model.ActiveStatus;

public record UserRepresentation(
    UUID id,
    String displayName,
    String login,
    LocalDateTime createdDate,
    ActiveStatus status
) {}
