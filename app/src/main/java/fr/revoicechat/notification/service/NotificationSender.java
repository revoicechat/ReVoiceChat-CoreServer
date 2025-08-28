package fr.revoicechat.notification.service;

import fr.revoicechat.notification.model.Notification;
import fr.revoicechat.notification.model.NotificationRegistrable;

public interface NotificationSender {
  /** Broadcast a message to all targeted users. */
  void send(Notification notification);

  /** Ping a single user. */
  boolean ping(NotificationRegistrable registrable);
}
