package fr.revoicechat.core.notification;

import static fr.revoicechat.core.notification.NotificationUserRetriever.findUserForRoom;
import static fr.revoicechat.notification.data.NotificationActionType.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.representation.MessageRepresentation;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.data.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.web.mapper.Mapper;

@NotificationType(name = "ROOM_MESSAGE")
@Schema(description = "Message notification")
public record MessageNotification(MessageRepresentation message, NotificationActionType action) implements NotificationPayload {
  public static void add(Message message) {
    MessageRepresentation representation = Mapper.map(message);
    notifyUpdate(new MessageNotification(representation, ADD));
  }

  public static void update(Message message) {
    MessageRepresentation representation = Mapper.map(message);
    notifyUpdate(new MessageNotification(representation, MODIFY));
  }

  public static void delete(Message message) {
    MessageRepresentation representation = Mapper.mapLight(message);
    notifyUpdate(new MessageNotification(representation, REMOVE));
  }

  private static void notifyUpdate(final MessageNotification message) {
    Notification.of(message).sendTo(findUserForRoom(message.message().roomId()));
  }
}