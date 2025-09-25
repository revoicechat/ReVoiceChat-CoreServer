package fr.revoicechat.risk.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.UUID;

import fr.revoicechat.risk.RisksMembership;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import fr.revoicechat.risk.retriever.ServerRoleIdRetriever;
import fr.revoicechat.risk.service.server.ServerRoleService;
import fr.revoicechat.risk.web.api.RoleController;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class RoleControllerImpl implements RoleController {

  private final ServerRoleService serverRoleService;

  public RoleControllerImpl(final ServerRoleService serverRoleService) {
    this.serverRoleService = serverRoleService;
  }

  @Override
  @RisksMembership
  @RisksMembershipData(risks = "UPDATE_ROLE", retriever = ServerRoleIdRetriever.class)
  public ServerRoleRepresentation updateRole(final UUID serverRoleId, final CreatedServerRoleRepresentation representation) {
    return serverRoleService.update(serverRoleId, representation);
  }
}
