package fr.revoicechat.moderation.representation;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.revoicechat.moderation.model.SanctionType;

public record SanctionRepresentation(UUID id,
                                     UUID targetedUser,
                                     UUID server,
                                     SanctionType type,
                                     LocalDateTime startAt,
                                     LocalDateTime expiresAt,
                                     UUID issuedBy,
                                     String reason,
                                     UUID revokedBy,
                                     LocalDateTime revokedAt,
                                     boolean active) {}
