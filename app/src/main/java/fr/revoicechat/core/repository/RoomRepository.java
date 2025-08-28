package fr.revoicechat.core.repository;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.Room;

public interface RoomRepository {
  List<Room> findByServerId(UUID serverId);
}
