package fr.revoicechat.core.repository;

import java.util.UUID;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.repository.page.PageResult;

public interface MessageRepository {
  PageResult<Message> findByRoomId(UUID roomId, int page, int size);
}
