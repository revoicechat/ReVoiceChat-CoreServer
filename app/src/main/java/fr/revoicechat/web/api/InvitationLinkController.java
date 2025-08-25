package fr.revoicechat.web.api;

import java.util.UUID;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.invitation.InvitationRepresentation;

@Tag(name = "Invitation", description = "Endpoints for server or application invitations")
@Path("invitation")
public interface InvitationLinkController extends LoggedApi {

  @Operation(summary = "Generate an invitation to join the application",
      description = "Generate an invitation to join the application.")
  @APIResponse(responseCode = "200", description = "invitation successfully generated",
      content = @Content(schema = @Schema(implementation = User.class))
  )
  @POST
  @Path("/application")
  InvitationRepresentation generateApplicationInvitation();

  @Operation(summary = "Revoke an unused invitation", description = "Revoke an unused invitation.")
  @APIResponse(responseCode = "200", description = "invitation successfully revoked",
      content = @Content(schema = @Schema(implementation = User.class))
  )
  @DELETE
  @Path("/{id}")
  void revoke(@PathParam("id") UUID id);
}
