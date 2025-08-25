package fr.revoicechat.representation.sse;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record SseData(SseTypeData type,
                      @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
                      SsePayload data) implements Serializable {
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
