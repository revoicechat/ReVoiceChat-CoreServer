package fr.revoicechat.voice.notification;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "VOICE_LEAVING")
@Schema(description = "Voice leaving notification")
public record VoiceLeavingNotification(UUID userId, UUID roomId) implements NotificationPayload {}
