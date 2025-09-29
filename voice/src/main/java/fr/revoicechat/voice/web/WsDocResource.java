package fr.revoicechat.voice.web;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Here for documentation purpose.
 *
 * @see fr.revoicechat.voice.socket.ChatWebSocket
 */
@Path("/voice")
@Tag(name = "WebSocket")
@Produces(MediaType.APPLICATION_JSON)
public class WsDocResource {

  @GET
  @Operation(summary = "WebSocket endpoint",
      description = """
          Connect via `ws://*url*/api/voice/{roomId}?token={jwtToken}`.
            - Text messages: JSON control
            - Binary messages: audio chunks
          @param roomId: id of the room. it must be of type "VOICE"
          @param token: needed to know which user is connected""")
  public Response wsInfo() {
    return Response.ok(Map.of("url", "ws://*url*/api/voice/{roomId}?token={jwtToken}")).build();
  }
}
