package fr.revoicechat.service.server;

import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.RoomType;
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
    serverRepository.save(server);
    createRoom(server, "📝 General",  RoomType.TEXT);
    createRoom(server, "📝 Random",   RoomType.TEXT);
    createRoom(server, "🔊 Vocal", RoomType.WEBRTC);
    return server;
  }

  private void createRoom(final Server server, final String name, RoomType type) {
    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setName(name);
    room.setServer(server);
    room.setType(type);
    roomRepository.save(room);
  }
}
