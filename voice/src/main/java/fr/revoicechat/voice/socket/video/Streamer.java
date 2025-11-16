package fr.revoicechat.voice.socket.video;

import java.util.Objects;
import java.util.UUID;

import jakarta.websocket.Session;

record Streamer(UUID user, String streamName, Session session) implements VideoUser {
    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Streamer that)) {
        return false;
      }
      return Objects.equals(user, that.user) && Objects.equals(streamName, that.streamName);
    }

    @Override
    public int hashCode() {
      int result = Objects.hashCode(user);
      result = 31 * result + Objects.hashCode(streamName);
      return result;
    }
  }