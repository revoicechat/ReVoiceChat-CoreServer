package fr.revoicechat.risk.web.api;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Tag(name = "Role", description = "Endpoints for managing roles")
@Path("role/{id}")
public interface RoleController {

  @Operation(summary = "Update a role", description = "Update a specific role")
  @APIResponse(responseCode = "200", description = "Role successfully updated")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @PATCH
  ServerRoleRepresentation updateRole(@PathParam("id") UUID serverId, CreatedServerRoleRepresentation representation);
}
