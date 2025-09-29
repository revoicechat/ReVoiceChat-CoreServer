package fr.revoicechat.voice.stub;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.voice.service.user.ConnectedUserRetriever;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConnectedUserRetrieverMock implements ConnectedUserRetriever {
  @Override
  public Stream<UUID> getConnectedUsers(final UUID room) {
    return Stream.empty();
  }
}
