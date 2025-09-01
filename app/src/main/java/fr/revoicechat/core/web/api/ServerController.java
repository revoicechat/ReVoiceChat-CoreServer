package fr.revoicechat.core.web.api;

import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;

@Path("server")
@Tag(name = "Server", description = "Endpoints for managing server and their rooms")
public interface ServerController extends LoggedApi {

  @Operation(summary = "Get all servers", description = "Retrieve the list of all available servers.")
  @APIResponse(responseCode = "200", description = "List of servers successfully retrieved")
  @GET
  List<ServerRepresentation> getServers();

  @Operation(summary = "Get a server by ID", description = "Retrieve details of a specific server by its unique identifier.")
  @APIResponse(responseCode = "200", description = "Server successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @GET
  @Path("/{id}")
  ServerRepresentation getServer(@PathParam("id") UUID id);

  @Operation(summary = "Create a new server", description = "Create a new server with the provided information.")
  @APIResponse(responseCode = "200", description = "Server successfully created")
  @APIResponse(
      responseCode = "400",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Invalid input data")
      )
  )
  @PUT
  ServerRepresentation createServer(ServerCreationRepresentation representation);

  @Operation(summary = "Update an existing server", description = "Update the information of an existing server by its ID.")
  @APIResponse(responseCode = "200", description = "Server successfully updated")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  ServerRepresentation updateServer(@PathParam("id") UUID id, ServerCreationRepresentation representation);

  @Operation(summary = "Update an existing server", description = "Update the information of an existing server by its ID.")
  @APIResponse(responseCode = "200", description = "Server successfully updated")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @APIResponse(
      responseCode = "400",
      description = "Server cannot be deleted",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server cannot be deleted")
      )
  )
  @DELETE
  @Path("/{id}")
  void deleteServer(@PathParam("id") UUID id);

  @Tags(refs = { "Server", "Room" })
  @Operation(summary = "Get rooms for a server", description = "Retrieve the list of rooms belonging to a specific server.")
  @APIResponse(responseCode = "200", description = "List of rooms successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @GET
  @Path("/{id}/room")
  List<Room> getRooms(@PathParam("id") final UUID id);

  @Tags(refs = { "Server", "Room" })
  @Operation(summary = "Create a new room in a server", description = "Add a new room to a specific server identified by its ID.")
  @APIResponse(responseCode = "200", description = "Room successfully created")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @PUT
  @Path("/{id}/room")
  Room createRoom(@PathParam("id") final UUID id, RoomRepresentation representation);

  @Tags(refs = { "Server", "User" })
  @Operation(summary = "Get all user for a server", description = "Retrieve the list of user using a specific server.")
  @APIResponse(responseCode = "200", description = "List of user successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @GET
  @Path("/{id}/user")
  List<UserRepresentation> fetchUsers(@PathParam("id") UUID id);
}
