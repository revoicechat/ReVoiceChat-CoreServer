package fr.revoicechat.core.representation.server;

import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "SERVER_UPDATE")
public record ServerUpdateNotification(ServerRepresentation server, NotificationActionType action) implements NotificationPayload {}