package fr.revoicechat.representation.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    LocalDateTime createdDate,
    ActionType actionType
) {

  public enum ActionType {
    ADD, MODIFY, REMOVE
  }
}
