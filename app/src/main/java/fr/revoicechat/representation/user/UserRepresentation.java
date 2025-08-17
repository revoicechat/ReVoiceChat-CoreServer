package fr.revoicechat.representation.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRepresentation(
    UUID id,
    String username,
    String login,
    LocalDateTime createdDate
) {
}
