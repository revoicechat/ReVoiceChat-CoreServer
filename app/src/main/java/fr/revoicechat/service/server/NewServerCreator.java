package fr.revoicechat.service.server;

import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.Server;
import fr.revoicechat.repository.RoomRepository;
import fr.revoicechat.repository.ServerRepository;

@Service
public class NewServerCreator {

  private final ServerRepository serverRepository;
  private final RoomRepository roomRepository;

  public NewServerCreator(final ServerRepository serverRepository, final RoomRepository roomRepository) {
    this.serverRepository = serverRepository;
    this.roomRepository = roomRepository;
  }

  public Server create(Server server) {
    server.setId(UUID.randomUUID());
    server.setName("server");
    serverRepository.save(server);
    createRoom(server, "General");
    createRoom(server, "Random");
    return server;
  }

  private void createRoom(final Server server, final String general) {
    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setName(general);
    room.setServer(server);
    roomRepository.save(room);
  }
}
