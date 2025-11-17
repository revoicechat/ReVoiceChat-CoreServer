package fr.revoicechat.live.stub;

import java.util.UUID;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.notification.model.NotificationRegistrable;
import fr.revoicechat.risk.service.server.ServerFinder;

@ApplicationScoped
public class ServerFinderMock implements ServerFinder {
  @Override
  public void existsOrThrow(final UUID id) {
    // nothing here
  }

  @Override
  public Stream<NotificationRegistrable> findUserForServer(final UUID serverId) {
    return Stream.of();
  }
}
