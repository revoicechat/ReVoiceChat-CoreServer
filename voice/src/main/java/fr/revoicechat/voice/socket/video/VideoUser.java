package fr.revoicechat.voice.socket.video;

import java.util.Objects;
import java.util.UUID;

import jakarta.websocket.Session;

public interface VideoUser {
  UUID user();
  Session session();
}
