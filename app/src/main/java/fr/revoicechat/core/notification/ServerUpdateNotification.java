package fr.revoicechat.core.notification;

import static fr.revoicechat.core.notification.NotificationUserRetriever.findUserForServer;
import static fr.revoicechat.notification.data.NotificationActionType.MODIFY;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.representation.ServerRepresentation;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.data.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.web.mapper.Mapper;

@NotificationType(name = "SERVER_UPDATE")
@Schema(description = "Server update notification")
public record ServerUpdateNotification(ServerRepresentation server,
                                       NotificationActionType action) implements NotificationPayload {

  public static void update(Server server) {
    notifyUpdate(new ServerUpdateNotification(Mapper.mapLight(server), MODIFY));
  }

  private static void notifyUpdate(final ServerUpdateNotification notification) {
    Notification.of(notification).sendTo(findUserForServer(notification.server().id()));
  }
}