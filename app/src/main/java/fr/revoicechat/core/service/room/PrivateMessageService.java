package fr.revoicechat.core.service.room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.room.PrivateMessageRoom;
import fr.revoicechat.core.repository.PrivateMessageRoomRepository;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PrivateMessageService {

  private final EntityManager entityManager;
  private final UserHolder userHolder;
  private final UserService userService;
  private final PrivateMessageRoomRepository privateMessageRoomRepository;
  private final RoomMapper roomMapper;
  private final MessageService messageService;

  public PrivateMessageService(final EntityManager entityManager, final UserHolder userHolder, final UserService userService, final PrivateMessageRoomRepository privateMessageRoomRepository, final RoomMapper roomMapper, final MessageService messageService) {
    this.entityManager = entityManager;
    this.userHolder = userHolder;
    this.userService = userService;
    this.privateMessageRoomRepository = privateMessageRoomRepository;
    this.roomMapper = roomMapper;
    this.messageService = messageService;
  }

  public List<RoomRepresentation> findAll() {
    return privateMessageRoomRepository.findByUserId(userHolder.getId())
                                       .map(roomMapper::map)
                                       .toList();
  }

  public RoomRepresentation get(final UUID roomId) {
    return Optional.ofNullable(entityManager.find(PrivateMessageRoom.class, roomId))
                   .map(roomMapper::map)
                   .orElseThrow(() -> new ResourceNotFoundException(PrivateMessageService.class, roomId));
  }

  public RoomRepresentation getDirectDiscussion(final UUID userId) {
    return Optional.ofNullable(getDirectDiscussion(userId, userHolder.getId()))
                   .map(roomMapper::map)
                   .orElseThrow(() -> new ResourceNotFoundException(PrivateMessageService.class, userId));
  }

  @Transactional
  public MessageRepresentation sendPrivateMessageTo(final UUID userId, final CreatedMessageRepresentation representation) {
    var room = getDirectDiscussion(userId, userHolder.getId());
    if (room == null) {
      room = new PrivateMessageRoom();
      room.setId(UUID.randomUUID());
      room.setUsers(List.of(userHolder.get(), userService.getUser(userId)));
      entityManager.persist(room);
    }
    return messageService.create(room.getId(), representation);
  }

  private PrivateMessageRoom getDirectDiscussion(UUID user1, UUID user2) {
    return privateMessageRoomRepository.getDirectDiscussion(user1, user2);
  }
}
