package fr.revoicechat.core.representation.message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import fr.revoicechat.notification.representation.UserNotificationRepresentation;

public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    UserNotificationRepresentation user,
    OffsetDateTime createdDate,
    List<MediaDataRepresentation> medias
) {}
