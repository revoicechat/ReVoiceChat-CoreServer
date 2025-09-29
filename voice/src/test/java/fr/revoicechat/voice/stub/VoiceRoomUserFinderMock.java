package fr.revoicechat.voice.stub;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.notification.model.NotificationRegistrable;
import fr.revoicechat.voice.service.user.VoiceRoomUserFinder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VoiceRoomUserFinderMock implements VoiceRoomUserFinder {
  @Override
  public Stream<NotificationRegistrable> find(final UUID room) {
    return Stream.empty();
  }
}
