package fr.revoicechat.core.service.user;

import java.util.UUID;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.notification.model.NotificationRegistrable;

@ApplicationScoped
public class RoomUserFinderImpl implements RoomUserFinder {

  private final UserRepository userRepository;

  public RoomUserFinderImpl(final UserRepository userRepository) {this.userRepository = userRepository;}

  @Override
  public Stream<NotificationRegistrable> find(UUID room) {
    return userRepository.findAll().stream().map(NotificationRegistrable.class::cast);
  }
}
