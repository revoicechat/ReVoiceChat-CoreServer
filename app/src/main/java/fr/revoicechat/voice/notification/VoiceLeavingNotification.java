package fr.revoicechat.voice.notification;

import java.util.UUID;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType("VOICE_LEAVING")
public record VoiceLeavingNotification(UUID userId) implements NotificationPayload {}
