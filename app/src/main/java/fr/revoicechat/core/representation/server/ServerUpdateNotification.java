package fr.revoicechat.core.representation.server;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "SERVER_UPDATE")
@Schema(description = "Server update notification")
public record ServerUpdateNotification(ServerRepresentation server, NotificationActionType action) implements NotificationPayload {}