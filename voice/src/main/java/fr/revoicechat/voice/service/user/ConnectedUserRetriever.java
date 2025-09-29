package fr.revoicechat.voice.service.user;

import java.util.UUID;
import java.util.stream.Stream;

public interface ConnectedUserRetriever {

  Stream<UUID> getConnectedUsers(UUID room);
}
