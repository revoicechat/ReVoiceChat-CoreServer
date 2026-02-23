package fr.revoicechat.core.notification;

import static fr.revoicechat.core.notification.NotificationUserRetriever.findUserForRoom;
import static fr.revoicechat.notification.data.NotificationActionType.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import fr.revoicechat.core.model.room.Room;
import fr.revoicechat.core.representation.RoomRepresentation;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.data.NotificationActionType;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.web.mapper.Mapper;

@NotificationType(name = "ROOM_UPDATE")
@Schema(description = "Room notification")
public record RoomNotification(RoomRepresentation room, NotificationActionType action) implements NotificationPayload {

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
    Notification.of(roomRepresentation).sendTo(findUserForRoom(roomRepresentation.room.id()));
  }
}