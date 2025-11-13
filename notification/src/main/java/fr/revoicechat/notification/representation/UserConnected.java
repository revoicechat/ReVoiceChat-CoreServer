package fr.revoicechat.notification.representation;


import java.util.UUID;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "USER_CONNECTED")
public record UserConnected(UUID userId) implements NotificationPayload {}
