package fr.revoicechat.core.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.impl.room.RoomUnreadSummary;

public interface RoomRepository {
  List<Room> findByServerId(UUID serverId);
  List<UUID> findIdThatAreNotInRoom(UUID serverId, List<UUID> ids);

  UUID getServerId(UUID room);

  Stream<Room> findRoomsByUserServers(UUID userId);

  RoomUnreadSummary findUnreadSummary(Room room, User currentUser);
}
