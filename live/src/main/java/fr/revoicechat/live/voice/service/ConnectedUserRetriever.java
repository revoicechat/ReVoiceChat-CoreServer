package fr.revoicechat.live.voice.service;

import java.util.UUID;
import java.util.stream.Stream;

public interface ConnectedUserRetriever {

  Stream<UUID> getConnectedUsers(UUID room);
  UUID getRoomForUser(UUID user);
}
