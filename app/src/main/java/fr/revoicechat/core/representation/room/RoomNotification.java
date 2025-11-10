package fr.revoicechat.core.representation.room;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "ROOM_UPDATE")
@Schema(description = "Room notification")
public record RoomNotification(RoomRepresentation room, NotificationActionType action) implements NotificationPayload {
}