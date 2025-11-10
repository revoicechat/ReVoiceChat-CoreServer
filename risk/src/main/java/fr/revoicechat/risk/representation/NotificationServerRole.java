package fr.revoicechat.risk.representation;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "RISK_MANAGEMENT")
@Schema(description = "risk management notification")
public record NotificationServerRole() implements NotificationPayload {}
