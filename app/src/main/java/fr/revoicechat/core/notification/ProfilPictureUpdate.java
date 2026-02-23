package fr.revoicechat.core.notification;

import static fr.revoicechat.core.notification.NotificationUserRetriever.findUserForServer;

import java.util.UUID;

import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType(name = "PROFIL_PICTURE_UPDATE")
public record ProfilPictureUpdate(UUID id) implements NotificationPayload {
  public static void serverUpdate(UUID id) {
    Notification.of(new ProfilPictureUpdate(id)).sendTo(findUserForServer(id));
  }

  public static void userUpdate(UUID id) {
    Notification.of(new ProfilPictureUpdate(id)).sendTo(findUserForServer(id));
  }
}
