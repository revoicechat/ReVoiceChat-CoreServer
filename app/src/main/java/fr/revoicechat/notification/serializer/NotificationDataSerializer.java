package fr.revoicechat.notification.serializer;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.revoicechat.notification.model.NotificationData;
import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import com.fasterxml.jackson.core.JsonGenerator;

public class NotificationDataSerializer extends JsonSerializer<NotificationData> {

  @Override
  public void serialize(NotificationData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    NotificationPayload payload = value.data();
    NotificationType annotation = Objects.requireNonNull(payload.getClass().getAnnotation(NotificationType.class));
    gen.writeStringField("type", annotation.value());
    gen.writeObjectField("data", payload);
    gen.writeEndObject();
  }
}
