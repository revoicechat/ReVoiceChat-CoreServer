package fr.revoicechat.core.web.api;

import java.util.UUID;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;

@Path("message/{id}")
@Tag(name = "Message", description = "Endpoints for managing chat messages")
public interface MessageController extends LoggedApi {

  @Operation(summary = "Get a message", description = "Retrieve a message by its unique identifier.")
  @APIResponse(responseCode = "200", description = "Message successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Message not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Message not found")
      )
  )
  @GET
  MessageRepresentation read(@PathParam("id") UUID id);

  @Operation(summary = "Update a message", description = "Partially update an existing message using its ID.")
  @APIResponse(responseCode = "200", description = "Message successfully updated")
  @APIResponse(responseCode = "404",
      description = "Message not found",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, examples = "Message not found"))
  )
  @PATCH
  MessageRepresentation update(@PathParam("id") UUID id, CreatedMessageRepresentation representation);

  @Operation(summary = "Delete a message", description = "Delete a message by its unique identifier.")
  @APIResponse(responseCode = "204", description = "Message successfully deleted")
  @APIResponse(
      responseCode = "404",
      description = "Message not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Message not found")
      )
  )
  @DELETE
  UUID delete(@PathParam("id") UUID id);
}
