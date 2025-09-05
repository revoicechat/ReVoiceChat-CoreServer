package fr.revoicechat.core.representation.message;

import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "ROOM_MESSAGE")
public record MessageNotification(MessageRepresentation message, NotificationActionType action) implements NotificationPayload {
}