package fr.revoicechat.voice.service.room;

import java.util.UUID;

public interface VoiceRoomPredicate {

  boolean isVoiceRoom(UUID roomId);
}
