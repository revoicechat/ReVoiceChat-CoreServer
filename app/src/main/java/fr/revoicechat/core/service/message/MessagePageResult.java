package fr.revoicechat.core.service.message;

import java.util.UUID;

import fr.revoicechat.core.repository.impl.MessageRepositoryImpl;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.MessageFilterParams;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MessagePageResult {

  private final MessageRepositoryImpl messageRepositoryImpl;

  public MessagePageResult(final MessageRepositoryImpl messageRepositoryImpl) {
    this.messageRepositoryImpl = messageRepositoryImpl;
  }

  /**
   * Retrieves all messages for a given chat room.
   *
   * @param roomId the unique identifier of the chat room
   * @return list of messages in the room, possibly empty if no messages exist
   */
  @Transactional
  public PageResult<MessageRepresentation> getMessagesByRoom(UUID roomId, MessageFilterParams params) {
    params.setRoomId(roomId);
    var pageResult = messageRepositoryImpl.search(params);
    return new PageResult<>(pageResult.content()
                                      .stream()
                                      .<MessageRepresentation>map(Mapper::map)
                                      .toList(),
        pageResult.pageNumber(), pageResult.pageSize(), pageResult.totalElements());
  }
}
