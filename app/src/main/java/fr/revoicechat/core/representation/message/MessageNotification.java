package fr.revoicechat.core.representation.message;

import static fr.revoicechat.notification.representation.NotificationActionType.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.enterprise.inject.spi.CDI;

@NotificationType(name = "ROOM_MESSAGE")
@Schema(description = "Message notification")
public record MessageNotification(MessageRepresentation message, NotificationActionType action) implements NotificationPayload {
  private static final ThreadLocal<RoomUserFinder> holder = new ThreadLocal<>();

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
    Notification.of(message).sendTo(getRoomUserFinder().find(message.message().roomId()));
  }

  private static RoomUserFinder getRoomUserFinder() {
    RoomUserFinder sender = holder.get();
    if (sender == null) {
      sender = CDI.current().select(RoomUserFinder.class).get();
      holder.set(sender);
    }
    return sender;
  }
}