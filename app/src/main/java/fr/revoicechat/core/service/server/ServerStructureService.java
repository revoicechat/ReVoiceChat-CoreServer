package fr.revoicechat.core.service.server;

import static fr.revoicechat.core.nls.ServerErrorCode.SERVER_STRUCTURE_WITH_ROOM_THAT_DOES_NOT_EXISTS;
import static fr.revoicechat.notification.representation.NotificationActionType.MODIFY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.mapper.ServerMapper;
import fr.revoicechat.core.model.room.Room;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerItem;
import fr.revoicechat.core.model.server.ServerRoomItem;
import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.core.repository.RoomRepository;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.server.ServerUpdateNotification;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.web.error.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerStructureService {

  private final ServerEntityService serverEntityService;
  private final EntityManager entityManager;
  private final RoomRepository roomRepository;
  private final RoomService roomService;
  private final UserRepository userRepository;
  private final ServerMapper serverMapper;

  public ServerStructureService(final ServerEntityService serverEntityService,
                                final EntityManager entityManager,
                                final RoomRepository roomRepository,
                                final RoomService roomService,
                                final UserRepository userRepository, final ServerMapper serverMapper) {
    this.serverEntityService = serverEntityService;
    this.entityManager = entityManager;
    this.roomRepository = roomRepository;
    this.roomService = roomService;
    this.userRepository = userRepository;
    this.serverMapper = serverMapper;
  }

  @Transactional
  public ServerStructure updateStructure(final UUID id, final ServerStructure structure) {
    List<UUID> roomId = structure == null ? List.of() : flatStructure(structure.items(), new ArrayList<>());
    if (!roomId.isEmpty() && !roomRepository.findIdThatAreNotInRoom(id, roomId).isEmpty()) {
      throw new BadRequestException(SERVER_STRUCTURE_WITH_ROOM_THAT_DOES_NOT_EXISTS);
    }
    var server = serverEntityService.getEntity(id);
    server.setStructure(structure);
    entityManager.persist(server);
    var newStructure = getStructure(id);
    Notification.of(new ServerUpdateNotification(serverMapper.mapLight(server), MODIFY)).sendTo(userRepository.findByServers(id));
    return newStructure;
  }

  public ServerStructure getStructure(final UUID id) {
    return Optional.ofNullable(serverEntityService.getEntity(id).getStructure())
                   .orElseGet(() -> new ServerStructure(new ArrayList<>(roomService.findAll(id).stream()
                                                                                   .map(Room::getId)
                                                                                   .map(ServerRoomItem::new)
                                                                                   .toList())));
  }

  private List<UUID> flatStructure(final List<ServerItem> structure, List<UUID> ids) {
    structure.forEach(item -> {
      switch (item) {
        case ServerRoomItem(UUID id) -> ids.add(id);
        case ServerCategory category -> flatStructure(category.items(), ids);
      }
    });
    return ids;
  }
}
