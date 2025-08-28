package fr.revoicechat.notification.model;

public enum NotificationType {
  // User
  PING,
  USER_UPDATE,
  // Server
  SERVER_UPDATE,
  ROOM_UPDATE,
  // Message
  ROOM_MESSAGE,
  DIRECT_MESSAGE,
  // Voice
  VOICE_ROOM_JOIN,
  VOICE_ROOM_LEAVE,
}