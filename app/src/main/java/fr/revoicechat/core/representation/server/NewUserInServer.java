package fr.revoicechat.core.representation.server;

import java.util.UUID;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "NEW_USER_IN_SERVER")
public record NewUserInServer(UUID server, UUID user)
    implements NotificationPayload {}
