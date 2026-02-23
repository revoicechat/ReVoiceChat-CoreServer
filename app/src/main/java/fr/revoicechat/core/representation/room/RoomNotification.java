package fr.revoicechat.core.representation.room;

import static fr.revoicechat.notification.representation.NotificationActionType.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.model.room.Room;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.enterprise.inject.spi.CDI;

@NotificationType(name = "ROOM_UPDATE")
@Schema(description = "Room notification")
public record RoomNotification(RoomRepresentation room, NotificationActionType action) implements NotificationPayload {
  private static final ThreadLocal<RoomUserFinder> holder = new ThreadLocal<>();

  public static void add(Room room) {
    RoomRepresentation representation = Mapper.mapLight(room);
    notifyUpdate(new RoomNotification(representation, ADD));
  }

  public static void update(Room room) {
    RoomRepresentation representation = Mapper.mapLight(room);
    notifyUpdate(new RoomNotification(representation, MODIFY));
  }

  public static void delete(Room room) {
    RoomRepresentation representation = new RoomRepresentation(room.getId(), null, null, null, null);
    notifyUpdate(new RoomNotification(representation, REMOVE));
  }

  private static void notifyUpdate(final RoomNotification roomRepresentation) {
    Notification.of(roomRepresentation).sendTo(getRoomUserFinder().find(roomRepresentation.room.id()));
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