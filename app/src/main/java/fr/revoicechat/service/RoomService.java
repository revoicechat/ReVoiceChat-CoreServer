package fr.revoicechat.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.revoicechat.model.Room;
import fr.revoicechat.repository.RoomRepository;

@Service
public class RoomService {

  private final RoomRepository repository;
  private final ServerService serverService;

  public RoomService(final RoomRepository repository, final ServerService serverService) {
    this.repository = repository;
    this.serverService = serverService;
  }

  public List<Room> findAll(final UUID id) {
    return repository.findByServerId(id);
  }

  public Room create(final UUID id, final Room room) {
    var server = serverService.get(id);
    room.setServer(server);
    return repository.save(room);
  }

  public Room get(final UUID roomId) {
    return repository.findById(roomId).orElseThrow();
  }
}
