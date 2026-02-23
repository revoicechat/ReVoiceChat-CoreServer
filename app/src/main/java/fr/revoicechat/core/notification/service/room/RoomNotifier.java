package fr.revoicechat.core.notification.service.room;

import static fr.revoicechat.core.notification.service.NotificationUserRetriever.findUserForRoom;
import static fr.revoicechat.notification.data.NotificationActionType.*;
import static fr.revoicechat.notification.data.NotificationActionType.REMOVE;

import fr.revoicechat.core.model.room.Room;
import fr.revoicechat.core.notification.RoomNotification;
import fr.revoicechat.core.representation.RoomRepresentation;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
@Transactional
public class RoomNotifier {

  public void add(Room room) {
    RoomRepresentation representation = Mapper.mapLight(room);
    notifyUpdate(new RoomNotification(representation, ADD));
  }

  public void update(Room room) {
    RoomRepresentation representation = Mapper.mapLight(room);
    notifyUpdate(new RoomNotification(representation, MODIFY));
  }

  public void delete(Room room) {
    RoomRepresentation representation = new RoomRepresentation(room.getId(), null, null, null, null);
    notifyUpdate(new RoomNotification(representation, REMOVE));
  }

  private void notifyUpdate(final RoomNotification roomRepresentation) {
    Notification.of(roomRepresentation).sendTo(findUserForRoom(roomRepresentation.room().id()));
  }
}
