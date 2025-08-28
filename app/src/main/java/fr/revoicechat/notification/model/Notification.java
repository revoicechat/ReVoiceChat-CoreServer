package fr.revoicechat.notification.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Notification {
  private List<UUID> targetedUsers;
  private NotificationData data;

  private Notification() {
    this.targetedUsers = new ArrayList<>();
  }

  public static Notification forUsers(Stream<? extends NotificationRegistrable> targetedUsers) {
    var sender = new Notification();
    sender.targetedUsers = targetedUsers.map(NotificationRegistrable::getId).toList();
    return sender;
  }

  public static Notification forUser(NotificationRegistrable targetedUser) {
    var sender = new Notification();
    sender.targetedUsers = List.of(targetedUser.getId());
    return sender;
  }

  public static Notification forUsers(List<UUID> targetedUsers) {
    var sender = new Notification();
    sender.targetedUsers = targetedUsers;
    return sender;
  }

  public static Notification forUser(UUID targetedUser) {
    var sender = new Notification();
    sender.targetedUsers = List.of(targetedUser);
    return sender;
  }

  public Notification withData(NotificationType type, NotificationPayload payload) {
    this.data = new NotificationData(type, payload);
    return this;
  }

  public void forEach(Consumer<UUID> action) {
    targetedUsers.forEach(action);
  }

  public NotificationData data() {
    return data;
  }
}
