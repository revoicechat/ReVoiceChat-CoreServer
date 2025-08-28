package fr.revoicechat.core.web.api;

import java.util.UUID;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;

@Path("room/{id}")
@Tag(name = "Room", description = "Endpoints for managing chat rooms and their messages")
public interface RoomController extends LoggedApi {

  @Operation(summary = "Get room details", description = "Retrieve the details of a room by its unique identifier.")
  @APIResponse(responseCode = "200", description = "Room successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Room not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Room not found")
      )
  )
  @GET
  Room read(@PathParam("id") UUID roomId);

  @Operation(summary = "Update a room", description = "Update the properties of an existing room using its ID.")
  @APIResponse(responseCode = "200", description = "Room successfully updated")
  @APIResponse(
      responseCode = "404",
      description = "Room not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Room not found")
      )
  )
  @PATCH
  Room update(@PathParam("id") UUID roomId, RoomRepresentation representation);

  @Operation(summary = "Delete a room", description = "Delete an existing room by its ID. Returns the deleted room's ID.")
  @APIResponse(responseCode = "200", description = "Room successfully deleted")
  @APIResponse(
      responseCode = "404",
      description = "Room not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Room not found")
      )
  )
  @DELETE
  UUID delete(@PathParam("id") UUID roomId);

  @Tags(refs = { "Room", "Message" })
  @Operation(summary = "Get messages in a room", description = "Retrieve all messages from a specific room.")
  @APIResponse(responseCode = "200", description = "Messages successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Room not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Room not found")
      )
  )
  @GET
  @Path("/message")
  PageResult<MessageRepresentation> messages(@PathParam("id") UUID roomId,
                                             @QueryParam("page") int page, // 0
                                             @QueryParam("size") int size); // 50

  @Tags(refs = { "Room", "Message" })
  @Operation(summary = "Send a message in a room", description = "Create and send a new message inside a specific room.")
  @APIResponse(responseCode = "200", description = "Message successfully created")
  @APIResponse(
      responseCode = "404",
      description = "Room not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Room not found")
      )
  )
  @PUT
  @Path("/message") MessageRepresentation sendMessage(@PathParam("id") UUID roomId, CreatedMessageRepresentation representation);
}
