package fr.revoicechat.core.web;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.service.invitation.InvitationLinkService;
import fr.revoicechat.core.web.api.InvitationLinkController;

@RolesAllowed("USER") // only authenticated users
public class InvitationLinkControllerImpl implements InvitationLinkController {
  private final InvitationLinkService invitationLinkService;

  public InvitationLinkControllerImpl(final InvitationLinkService invitationLinkService) {this.invitationLinkService = invitationLinkService;}

  @Override
  public InvitationRepresentation generateApplicationInvitation() {
    return invitationLinkService.generateApplicationInvitation();
  }

  @Override
  public void revoke(final UUID id) {
    invitationLinkService.revoke(id);
  }
}
