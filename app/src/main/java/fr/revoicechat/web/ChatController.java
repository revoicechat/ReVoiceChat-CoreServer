package fr.revoicechat.web;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.SseElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.sse.TextualChatService;
import fr.revoicechat.web.api.LoggedApi;

@Path("/sse")
@Tag(name = "Chat", description = "Endpoints for real-time chat using Server-Sent Events (SSE)")
public class ChatController implements LoggedApi {
  private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

  private final TextualChatService textualChatService;
  private final UserHolder userHolder;

  public ChatController(TextualChatService textualChatService, UserHolder userHolder) {
    this.textualChatService = textualChatService;
    this.userHolder = userHolder;
  }

  @Operation(summary = "Register a user in the notification center",
      description = "Register a user in the notification center. a user can be registered multiple times")
  @APIResponse(responseCode = "200", description = "SSE stream successfully opened")
  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.APPLICATION_JSON)
  @RolesAllowed("USER") // only authenticated users
  public void generateSseEmitter(@Context Sse sse, @Context SseEventSink sink) {
    var user = userHolder.get();
    textualChatService.register(user.getId(), sse, sink);
    LOG.debug("sse connection for user {}", user.getId());
  }

  @OPTIONS
  @PermitAll
  public Response corsPreflight() {
    var allow = "HEAD, GET, OPTIONS";
    return Response.ok()
                   .header("access-control-allow-origin", "*")
                   .header("access-control-allow-headers", "*")
                   .header("Access-Control-Allow-Credentials", "true")
                   .header(HttpHeaders.ALLOW, allow)
                   .header(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8")
                   .entity(allow)
                   .build();
  }
}
