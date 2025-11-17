package fr.revoicechat.live.voice.web;

import java.util.Map;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.live.voice.socket.VoiceWebSocket;

/**
 * Here for documentation purpose.
 *
 * @see VoiceWebSocket
 */
@Path("/ws")
@Tag(name = "WebSocket")
@Produces(MediaType.APPLICATION_JSON)
public class WsDocResource {

  @GET
  @Path("/voice")
  @Operation(summary = "WebSocket endpoint for voice",
      description = """
          Connect via `ws://*url*/api/voice/{roomId}?token={jwtToken}`.
            - Text messages: JSON control
            - Binary messages: audio chunks
          @param roomId: id of the room. it must be of type "VOICE"
          @param token: needed to know which user is connected. it can also be given in a subsubject for mor secure connection""")
  public Response wsVoiceInfo() {
    return Response.ok(Map.of("url", "ws://*url*/api/voice/{roomId}?token={jwtToken}")).build();
  }
}
