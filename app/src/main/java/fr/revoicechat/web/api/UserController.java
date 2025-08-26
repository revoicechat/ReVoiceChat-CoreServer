package fr.revoicechat.web.api;

import java.util.UUID;

import fr.revoicechat.representation.user.AdminUpdatableUserData;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.representation.user.UpdatableUserData;
import fr.revoicechat.representation.user.UserRepresentation;
import jakarta.ws.rs.core.MediaType;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User", description = "Endpoints for managing user")
@Tag(name = "User", description = "Endpoints for managing user")
public interface UserController extends LoggedApi {

  @Operation(summary = "Get details of the connected user", description = "Retrieve the details of the specific connected user.")
  @APIResponse(responseCode = "200")
  @GET
  @Path("/me")
  UserRepresentation me();

  @Operation(summary = "Update details of the connected user",
      description = """
          Update the details of the specific connected user.
          Only no null data are updated""")
  @APIResponse(responseCode = "200")
  @PATCH
  @Path("/me")
  UserRepresentation me(UpdatableUserData userData);

  @Operation(summary = "Get details of a user", description = "Retrieve the details of a specific user by its id.")
  @APIResponse(responseCode = "200")
  @APIResponse(responseCode = "404",
      description = "User not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "User not found"))
  )
  @GET
  @Path("/{id}")
  UserRepresentation get(@PathParam("id") UUID id);

  @Operation(summary = "Update details of a user",
      description = """
    Update specifics details of a specific user by its id.
    only display name and user type (USER/BOT/ADMIN) can be updated by this method.
    """)
  @APIResponse(responseCode = "200")
  @APIResponse(responseCode = "404",
      description = "User not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "User not found"))
  )
  @PATCH
  @Path("/{id}")
  UserRepresentation updateAsAdmin(@PathParam("id") UUID id, AdminUpdatableUserData userData);
}
