package fr.revoicechat.notification.web;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.reflections.Reflections;

import fr.revoicechat.notification.model.NotificationPayload;
import fr.revoicechat.notification.model.NotificationType;
import fr.revoicechat.openapi.OpenAPIFilterer;

public class NotificationOpenAPIFilterer implements OpenAPIFilterer {

  @Override
  public void filterOpenAPI(OpenAPI openAPI) {
    var pathItem = openAPI.getPaths().getPathItem("/api/sse");
    if (pathItem == null) {
      return;
    }
    var response = pathItem.getGET()
                           .getResponses()
                           .getAPIResponse("200")
                           .getContent()
                           .getMediaType("application/json");
    // Scan the classpath at runtime
    Reflections reflections = new Reflections("fr.revoicechat"); // your base package
    Set<Class<? extends NotificationPayload>> payloads = reflections.getSubTypesOf(NotificationPayload.class);
    Schema dataSchema = OASFactory.createSchema();
    dataSchema.setOneOf(payloads.stream()
                                .map(c -> {
                                  Schema ref = OASFactory.createSchema();
                                  ref.setRef("#/components/schemas/" + c.getSimpleName());
                                  return ref;
                                })
                                .toList());
    Schema wrapper = OASFactory.createSchema();
    wrapper.setType(List.of(SchemaType.OBJECT));
    // "type" field
    Schema typeField = OASFactory.createSchema();
    typeField.setType(List.of(SchemaType.STRING));
    typeField.setEnumeration(payloads.stream()
                                   .map(clazz -> clazz.getAnnotation(NotificationType.class))
                                   .filter(Objects::nonNull)
                                   .map(NotificationType::name)
                                   .collect(Collectors.toUnmodifiableList()));
    wrapper.addProperty("type", typeField);
    // "data" field
    wrapper.addProperty("data", dataSchema);
    response.setSchema(wrapper);
  }
}
