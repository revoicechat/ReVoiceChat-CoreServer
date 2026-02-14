package fr.revoicechat.core.repository;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.page.PageResult;

public interface MessageRepository {
  PageResult<Message> findByRoomId(UUID roomId, int page, int size);

  Stream<Message> findByRoom(Room room);

  Message findByMedia(UUID mediaId);
}
