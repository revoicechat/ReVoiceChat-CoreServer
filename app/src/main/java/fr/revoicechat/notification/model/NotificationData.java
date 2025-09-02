package fr.revoicechat.notification.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = NotificationDataSerializer.class)
public record NotificationData(NotificationPayload data) implements Serializable {

  public static NotificationData ping() {
    return new NotificationData(new Ping());
  }

  @NotificationType("PING")
  record Ping() implements NotificationPayload {}
}
