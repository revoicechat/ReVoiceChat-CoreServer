package fr.revoicechat.risk.service.server;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.notification.model.NotificationRegistrable;

public interface ServerFinder {

  void existsOrThrow(UUID id);

  Stream<NotificationRegistrable> findUserForServer(UUID serverId);
}
