package fr.revoicechat.core.representation.emote;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "EMOTE_UPDATE")
@Schema(description = "emote notification")
public record EmoteNotification(EmoteRepresentation message,
                                NotificationActionType action) implements NotificationPayload {}