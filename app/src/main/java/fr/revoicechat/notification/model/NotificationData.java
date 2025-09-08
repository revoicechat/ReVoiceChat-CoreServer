package fr.revoicechat.notification.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = NotificationDataSerializer.class)
public record NotificationData(NotificationPayload data) {

  public static NotificationData ping() {
    return new NotificationData(new Ping());
  }

  @NotificationType(name = "PING")
  public static final class Ping implements NotificationPayload {
    public Ping() {super();}
  }
}
