package fr.revoicechat.core.representation.room;

import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "ROOM_UPDATE")
public record RoomNotification(RoomRepresentation room, NotificationActionType action) implements NotificationPayload {
}