package fr.revoicechat.core.service.message;

import java.util.UUID;

import fr.revoicechat.core.repository.impl.MessageRepositoryImpl;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.MessageFilterParams;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.service.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MessagePageResult {

  private final MessageRepositoryImpl messageRepositoryImpl;
  private final MessageService messageService;

  public MessagePageResult(final MessageRepositoryImpl messageRepositoryImpl, final MessageService messageService) {
    this.messageRepositoryImpl = messageRepositoryImpl;
    this.messageService = messageService;
  }

  /**
   * Retrieves all messages for a given chat room.
   *
   * @param roomId the unique identifier of the chat room
   * @return list of messages in the room, possibly empty if no messages exist
   */
  @Transactional
  public PageResult<MessageRepresentation> getMessagesByRoom(UUID roomId, MessageFilterParams params) {
    var pageResult = messageRepositoryImpl.findByRoomId(roomId, params);
    return new PageResult<>(pageResult.content()
                                      .stream()
                                      .map(messageService::toRepresentation)
                                      .toList(),
        pageResult.pageNumber(), pageResult.pageSize(), pageResult.totalElements());
  }
}
