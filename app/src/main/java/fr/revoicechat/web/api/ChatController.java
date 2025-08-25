package fr.revoicechat.web.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.SseElementType;

import fr.revoicechat.representation.sse.SseData;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;

@Path("/sse")
@Tag(name = "Chat", description = "Endpoints for real-time chat using Server-Sent Events (SSE)")
public interface ChatController extends LoggedApi {

  @Operation(
      summary = "Subscribe to chat messages (SSE)",
      description = "Opens a Server-Sent Events (SSE) connection to receive chat messages in real time. "
                    + "The connection will continuously stream messages without needing repeated requests."
  )
  @APIResponse(responseCode = "200", description = "SSE stream successfully opened")
  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.APPLICATION_JSON)
  @Blocking
  Multi<SseData> generateSseEmitter();
}
