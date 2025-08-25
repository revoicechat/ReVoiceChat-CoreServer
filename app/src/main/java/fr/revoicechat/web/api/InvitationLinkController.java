package fr.revoicechat.web.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.invitation.InvitationRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Invitation",
    description = "Endpoints for server or application invitations"
)
@RequestMapping("invitation")
public interface InvitationLinkController extends LoggedApi {

  @PostMapping("/application")
  @Operation(
      summary = "Generate an invitation to join the application",
      description = "Generate an invitation to join the application.",
      responses = {
          @ApiResponse(responseCode = "200", description = "invitation successfully generated",
              content = @Content(schema = @Schema(implementation = User.class))
          )
      }
  )
  InvitationRepresentation generateApplicationInvitation();

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Revoke an unused invitation",
      description = "Revoke an unused invitation.",
      responses = {
          @ApiResponse(responseCode = "200", description = "invitation successfully revoked",
              content = @Content(schema = @Schema(implementation = User.class))
          )
      }
  )
  void revoke(@PathVariable("id") UUID id);
}
