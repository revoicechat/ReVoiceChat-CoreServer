package fr.revoicechat.core.service.user;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.live.voice.service.VoiceRoomUserFinder;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.notification.model.NotificationRegistrable;

@ApplicationScoped
public class RoomUserFinderImpl implements RoomUserFinder, VoiceRoomUserFinder {

  private final UserRepository userRepository;

  public RoomUserFinderImpl(final UserRepository userRepository) {this.userRepository = userRepository;}

  @Override
  public Stream<NotificationRegistrable> find(UUID room) {
    return userRepository.findByRoom(room).map(NotificationRegistrable.class::cast);
  }
}
