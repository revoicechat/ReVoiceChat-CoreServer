package fr.revoicechat.voice.stub;

import java.util.UUID;

import fr.revoicechat.voice.service.room.VoiceRoomPredicate;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VoiceRoomPredicateMock implements VoiceRoomPredicate {
  @Override
  public boolean isVoiceRoom(final UUID roomId) {
    return true;
  }
}
