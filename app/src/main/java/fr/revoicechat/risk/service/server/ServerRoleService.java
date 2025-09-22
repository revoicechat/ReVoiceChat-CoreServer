package fr.revoicechat.risk.service.server;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ServerRoleService {

  private final EntityManager entityManager;

  public ServerRoleService(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public List<ServerRoleRepresentation> getByServer(UUID serverId) {
    return entityManager.createQuery("select r from ServerRoles r where r.server = :serverId", ServerRoles.class)
                        .setParameter("serverId", serverId)
                        .getResultStream()
                        .map(this::mapToRepresentation)
                        .toList();
  }

  public ServerRoleRepresentation mapToRepresentation(ServerRoles roles) {
    return new ServerRoleRepresentation(
        roles.getId(),
        roles.getName(),
        roles.getColor(),
        roles.getServer()
    );
  }
}
