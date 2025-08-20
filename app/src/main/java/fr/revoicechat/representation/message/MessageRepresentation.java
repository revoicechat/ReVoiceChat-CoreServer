package fr.revoicechat.representation.message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    UserMessageRepresentation user,
    OffsetDateTime createdDate,
    ActionType actionType,
    List<MediaDataRepresentation> medias
) {

  public record UserMessageRepresentation(UUID id, String displayName) {}

  public enum ActionType {
    ADD, MODIFY, REMOVE
  }
}
