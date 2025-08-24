package fr.revoicechat.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.representation.invitation.InvitationRepresentation;
import fr.revoicechat.service.invitation.InvitationLinkService;
import fr.revoicechat.web.api.InvitationLinkController;

@RestController
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
