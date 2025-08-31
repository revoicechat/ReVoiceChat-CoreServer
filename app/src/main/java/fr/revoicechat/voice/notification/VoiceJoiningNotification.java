package fr.revoicechat.voice.notification;

import java.util.UUID;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType("VOICE_JOINING")
public record VoiceJoiningNotification(UUID userId) implements NotificationPayload {}
