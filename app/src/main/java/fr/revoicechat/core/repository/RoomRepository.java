package fr.revoicechat.core.repository;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.Room;

public interface RoomRepository {
  List<Room> findByServerId(UUID serverId);
  List<UUID> findIdThatAreNotInRoom(UUID serverId, List<UUID> ids);

  UUID getServerId(UUID room);
}
