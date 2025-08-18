package fr.revoicechat.representation.sse;

public record SseData(SseTypeData type, Object data) {
  public SseData(final SseTypeData type) {
    this(type, null);
  }

  public enum SseTypeData {
    PING,
    ROOM_MESSAGE,
    DIRECT_MESSAGE,
    USER_STATUS_CHANGE;
  }
}
