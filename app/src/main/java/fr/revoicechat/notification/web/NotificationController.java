package fr.revoicechat.notification.web;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.SseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.web.api.LoggedApi;
import fr.revoicechat.notification.NotificationRegistrableHolder;
import fr.revoicechat.notification.service.NotificationRegistry;

@PermitAll
@Path("/sse")
@Tag(name = "Notification", description = "Endpoints for real-time notification using Server-Sent Events (SSE)")
public class NotificationController implements LoggedApi {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);

  private final NotificationRegistry notificationRegistry;
  private final NotificationRegistrableHolder holder;

  public NotificationController(NotificationRegistry notificationRegistry, NotificationRegistrableHolder holder) {
    this.notificationRegistry = notificationRegistry;
    this.holder = holder;
  }

  @Operation(summary = "Register a user in the notification center",
      description = "Register a user in the notification center. a user can be registered multiple times")
  @APIResponse(responseCode = "200", description = "SSE stream successfully opened")
  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.APPLICATION_JSON)
  @RolesAllowed("USER") // only authenticated users
  public void generateSseEmitter(@Context SseEventSink sink) {
    var user = holder.get();
    notificationRegistry.register(user, sink);
    LOG.info("sse connection for user {}", user.getId());
  }
}
