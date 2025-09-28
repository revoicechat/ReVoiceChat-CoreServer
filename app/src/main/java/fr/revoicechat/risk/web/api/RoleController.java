package fr.revoicechat.risk.web.api;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Tag(name = "Role", description = "Endpoints for managing roles")
@Path("role/{id}")
public interface RoleController {

  @Operation(summary = "Update a role", description = "Update a specific role")
  @APIResponse(responseCode = "200", description = "Role successfully updated")
  @APIResponse(
      responseCode = "404",
      description = "Role not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Role not found")
      )
  )
  @PATCH
  ServerRoleRepresentation updateRole(@PathParam("id") UUID roleId, CreatedServerRoleRepresentation representation);

  @Operation(summary = "Add a role to a user", description = "Add a specific role to a list of users")
  @APIResponse(responseCode = "200", description = "Role successfully added")
  @APIResponse(
      responseCode = "404",
      description = "Role not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Role not found")
      )
  )
  @Path("user")
  @PUT
  void addRoleToUser(@PathParam("id") UUID roleId, List<UUID> users);

  @Operation(summary = "Get a role", description = "Get a role")
  @APIResponse(responseCode = "200", description = "Role successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Role not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Role not found")
      )
  )
  @GET
  ServerRoleRepresentation addRoleToUser(@PathParam("id") UUID roleId);
}
