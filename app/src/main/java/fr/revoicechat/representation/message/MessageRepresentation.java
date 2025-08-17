package fr.revoicechat.representation.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    UserMessageRepresentation user,
    LocalDateTime createdDate,
    ActionType actionType
) {

  public record UserMessageRepresentation(UUID id, String username) {}

  public enum ActionType {
    ADD, MODIFY, REMOVE
  }
}
