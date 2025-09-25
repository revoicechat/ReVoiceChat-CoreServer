package fr.revoicechat.risk.service.server;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.risk.model.Risk;
import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.repository.RiskRepository;
import fr.revoicechat.risk.repository.ServerRolesRepository;
import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerRoleService {

  private final RiskRepository riskRepository;
  private final ServerRolesRepository serverRolesRepository;
  private final EntityManager entityManager;
  private final ServerService serverService;

  public ServerRoleService(final RiskRepository riskRepository, final ServerRolesRepository serverRolesRepository, final EntityManager entityManager, final ServerService serverService) {
    this.riskRepository = riskRepository;
    this.serverRolesRepository = serverRolesRepository;
    this.entityManager = entityManager;
    this.serverService = serverService;
  }

  public List<ServerRoleRepresentation> getByServer(UUID serverId) {
    return serverRolesRepository.getByServer(serverId).map(this::mapToRepresentation).toList();
  }

  @Transactional
  public ServerRoleRepresentation create(final UUID serverId, final CreatedServerRoleRepresentation representation) {
    serverService.getEntity(serverId);
    ServerRoles roles = new ServerRoles();
    roles.setId(UUID.randomUUID());
    roles.setServer(serverId);
    mapBasicAttributes(representation, roles);
    entityManager.persist(roles);
    mapRisks(representation, roles);
    return mapToRepresentation(roles);
  }

  @Transactional
  public ServerRoleRepresentation update(final UUID serverRoleId, final CreatedServerRoleRepresentation representation) {
    ServerRoles roles = entityManager.getReference(ServerRoles.class, serverRoleId);
    if (roles == null) {
      throw new ResourceNotFoundException(ServerRoles.class, serverRoleId);
    }
    mapBasicAttributes(representation, roles);
    entityManager.persist(roles);
    riskRepository.getRisks(serverRoleId).forEach(entityManager::remove);
    mapRisks(representation, roles);
    return mapToRepresentation(roles);
  }

  private void mapBasicAttributes(final CreatedServerRoleRepresentation representation, final ServerRoles roles) {
    roles.setPriority(representation.priority());
    roles.setColor(representation.color());
    roles.setName(representation.name());
  }

  private void mapRisks(final CreatedServerRoleRepresentation representation, final ServerRoles roles) {
    representation.risks().forEach(risk -> {
      var newRisk = new Risk();
      newRisk.setId(UUID.randomUUID());
      newRisk.setEntity(risk.entity());
      newRisk.setMode(risk.mode());
      newRisk.setType(risk.type());
      newRisk.setServerRoles(roles);
      entityManager.persist(newRisk);
    });
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
