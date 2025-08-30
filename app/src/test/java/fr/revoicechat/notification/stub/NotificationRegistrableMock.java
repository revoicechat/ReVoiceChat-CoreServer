package fr.revoicechat.notification.stub;

import java.util.UUID;

import fr.revoicechat.notification.model.NotificationRegistrable;

public record NotificationRegistrableMock(UUID getId) implements NotificationRegistrable {}