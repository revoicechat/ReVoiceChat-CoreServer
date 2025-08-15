package fr.revoicechat.representation.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    UserRepresentation user,
    LocalDateTime createdDate,
    ActionType actionType
) {

  public record UserRepresentation(UUID id, String username) {}

  public enum ActionType {
    ADD, MODIFY, REMOVE
  }
}
