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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Tag(name = "Role", description = "Endpoints for managing roles")
@Path("server/{id}/role")
public interface ServerRoleController {

  @Operation(summary = "Get role of a server", description = "Get role list of a specific server")
  @APIResponse(responseCode = "200", description = "Role successfully retrieved")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @GET
  List<ServerRoleRepresentation> getByServer(@PathParam("id") UUID serverId);

  @Operation(summary = "Create role of a server", description = "Create a role for specific server")
  @APIResponse(responseCode = "200", description = "Role successfully created")
  @APIResponse(
      responseCode = "404",
      description = "Server not found",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, examples = "Server not found")
      )
  )
  @PUT
  ServerRoleRepresentation createRole(@PathParam("id") UUID serverId, CreatedServerRoleRepresentation representation);
}
