package fr.revoicechat.live.stub;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.live.voice.service.ConnectedUserRetriever;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConnectedUserRetrieverMock implements ConnectedUserRetriever {
  @Override
  public Stream<UUID> getConnectedUsers(final UUID room) {
    return Stream.empty();
  }

  @Override
  public UUID getRoomForUser(final UUID user) {
    return UUID.randomUUID();
  }
}
