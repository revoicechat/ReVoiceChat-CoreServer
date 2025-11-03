package fr.revoicechat.risk.web.api;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import fr.revoicechat.risk.type.RiskType;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Tags(refs = {"Role", "User"})
@Path("user")
public interface UserMembershipController {

  @Operation(summary = "Get role of self user", description = "Get role list of the connected user")
  @APIResponse(responseCode = "200", description = "Role successfully retrieved")
  @Path("/me/role")
  @GET
  List<ServerRoleRepresentation> getMyRolesMembership();

  @Operation(summary = "Get role of self user", description = "Get role list of the connected user")
  @APIResponse(responseCode = "200", description = "Role successfully retrieved")
  @Path("/server/{id}/risks")
  @GET
  List<RiskType> getMyRiskType(@PathParam("id") UUID serverId);
}
