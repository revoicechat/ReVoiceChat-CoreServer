package fr.revoicechat.core.repository;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.MessageFilterParams;

public interface MessageRepository {
  PageResult<Message> findByRoomId(UUID roomId, MessageFilterParams params);

  Stream<Message> findByRoom(Room room);

  Message findByMedia(UUID mediaId);
}
