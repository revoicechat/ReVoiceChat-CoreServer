package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.*;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.invitation.InvitationCategory;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.service.invitation.InvitationLinkService;
import fr.revoicechat.core.web.api.InvitationLinkController;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class InvitationLinkControllerImpl implements InvitationLinkController {
  private final InvitationLinkService invitationLinkService;

  public InvitationLinkControllerImpl(final InvitationLinkService invitationLinkService) {
    this.invitationLinkService = invitationLinkService;
  }

  @Override
  public InvitationRepresentation generateApplicationInvitation(final String category) {
    return invitationLinkService.generateApplicationInvitation(InvitationCategory.of(category));
  }

  @Override
  public InvitationRepresentation generateServerInvitation(final UUID serverId, final String category) {
    return invitationLinkService.generateServerInvitation(serverId, InvitationCategory.of(category));
  }

  @Override
  public List<InvitationRepresentation> getAllServerInvitations(final UUID serverId) {
    return invitationLinkService.getAllServerInvitations(serverId);
  }

  @Override
  public InvitationRepresentation get(UUID id) {
    return invitationLinkService.get(id);
  }

  @Override
  public List<InvitationRepresentation> getAll() {
    return invitationLinkService.getAllFromUser();
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public List<InvitationRepresentation> getAllApplicationInvitations() {
    return invitationLinkService.getAllApplicationInvitations();
  }

  @Override
  public void revoke(final UUID id) {
    invitationLinkService.revoke(id);
  }
}
