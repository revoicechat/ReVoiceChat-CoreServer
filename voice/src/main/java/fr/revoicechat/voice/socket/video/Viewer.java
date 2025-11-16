package fr.revoicechat.voice.socket.video;

import java.util.UUID;

import jakarta.websocket.Session;

record Viewer(UUID user, VideoRisks risks, Session session) implements VideoUser {
  record VideoRisks(boolean join, boolean send, boolean receive) {}
}