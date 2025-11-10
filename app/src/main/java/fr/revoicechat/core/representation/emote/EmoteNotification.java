package fr.revoicechat.core.representation.emote;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "EMOTE_UPDATE")
@Schema(description = "emote notification")
public record EmoteNotification(EmoteRepresentation emote,
                                UUID entity,
                                NotificationActionType action) implements NotificationPayload {}