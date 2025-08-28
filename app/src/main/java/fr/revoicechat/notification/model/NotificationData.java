package fr.revoicechat.notification.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record NotificationData(NotificationType type,
                               @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
                               NotificationPayload data) implements Serializable {
  public NotificationData(final NotificationType type) {
    this(type, null);
  }
}
