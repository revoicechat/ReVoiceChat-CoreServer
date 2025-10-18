package fr.revoicechat.risk.service.membership;

import java.util.List;

import fr.revoicechat.risk.model.UserRoleMembership;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import fr.revoicechat.risk.service.server.ServerRoleService;
import fr.revoicechat.security.UserHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class UserMembershipService {

  private final ServerRoleService serverRoleService;
  private final UserHolder userHolder;
  private final EntityManager entityManager;

  @Inject
  public UserMembershipService(final ServerRoleService serverRoleService, final UserHolder userHolder, final EntityManager entityManager) {
    this.serverRoleService = serverRoleService;
    this.userHolder = userHolder;
    this.entityManager = entityManager;
  }

  public List<ServerRoleRepresentation> getMyRolesMembership() {
    var membership = entityManager.find(UserRoleMembership.class, userHolder.getId());
    return membership.getServerRoles().stream()
                     .map(serverRoleService::mapToRepresentation)
                     .toList();
  }
}
