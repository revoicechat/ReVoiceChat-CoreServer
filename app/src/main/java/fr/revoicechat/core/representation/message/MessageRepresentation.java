package fr.revoicechat.core.representation.message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;

@NotificationType("ROOM_MESSAGE")
public record MessageRepresentation(
    UUID id,
    String text,
    UUID roomId,
    UserMessageRepresentation user,
    OffsetDateTime createdDate,
    ActionType actionType,
    List<MediaDataRepresentation> medias
) implements NotificationPayload {

  public record UserMessageRepresentation(UUID id, String displayName) {}

  public enum ActionType {
    ADD, MODIFY, REMOVE
  }
}
