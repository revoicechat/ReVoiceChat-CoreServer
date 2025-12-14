package fr.revoicechat.core.service.server;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.notification.model.NotificationRegistrable;
import fr.revoicechat.risk.service.user.UserServerFinder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserServerFinderImpl implements UserServerFinder {

  private final ServerProviderService serverProviderService;

  public UserServerFinderImpl(ServerProviderService serverProviderService) {
    this.serverProviderService = serverProviderService;
  }

  @Override
  public Stream<NotificationRegistrable> findUserForServer(final UUID serverId) {
    return serverProviderService.getUsers(serverId).map(NotificationRegistrable.class::cast);
  }
}
