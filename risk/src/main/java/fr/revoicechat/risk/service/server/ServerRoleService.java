package fr.revoicechat.risk.service.server;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.risk.model.Risk;
import fr.revoicechat.risk.model.RiskMode;
import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.model.UserRoleMembership;
import fr.revoicechat.risk.repository.RiskRepository;
import fr.revoicechat.risk.repository.ServerRolesRepository;
import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.NotificationServerRole;
import fr.revoicechat.risk.representation.RiskRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerRoleService {

  private final RiskRepository riskRepository;
  private final ServerRolesRepository serverRolesRepository;
  private final EntityManager entityManager;
  private final ServerFinder serverFinder;

  public ServerRoleService(RiskRepository riskRepository,
                           ServerRolesRepository serverRolesRepository,
                           EntityManager entityManager,
                           ServerFinder serverFinder) {
    this.riskRepository = riskRepository;
    this.serverRolesRepository = serverRolesRepository;
    this.entityManager = entityManager;
    this.serverFinder = serverFinder;
  }

  @Transactional
  public List<ServerRoleRepresentation> getByServer(UUID serverId) {
    return serverRolesRepository.getByServer(serverId).map(this::mapToRepresentation).toList();
  }

  public ServerRoleRepresentation get(final UUID roleId) {
    return mapToRepresentation(getEntity(roleId));
  }

  @Transactional
  public ServerRoleRepresentation create(final UUID serverId, final CreatedServerRoleRepresentation representation) {
    serverFinder.existsOrThrow(serverId);
    ServerRoles roles = new ServerRoles();
    roles.setId(UUID.randomUUID());
    roles.setServer(serverId);
    mapBasicAttributes(representation, roles);
    entityManager.persist(roles);
    mapRisks(representation, roles);
    var roleRepresentation = mapToRepresentation(roles);
    Notification.of(new NotificationServerRole(roleRepresentation, NotificationActionType.ADD)).sendTo(serverFinder.findUserForServer(serverId));
    return roleRepresentation;
  }

  @Transactional
  public ServerRoleRepresentation update(final UUID serverRoleId, final CreatedServerRoleRepresentation representation) {
    ServerRoles roles = getEntity(serverRoleId);
    mapBasicAttributes(representation, roles);
    entityManager.persist(roles);
    riskRepository.getRisks(serverRoleId).forEach(entityManager::remove);
    mapRisks(representation, roles);
    var roleRepresentation = mapToRepresentation(roles);
    Notification.of(new NotificationServerRole(roleRepresentation, NotificationActionType.MODIFY)).sendTo(serverFinder.findUserForServer(roles.getServer()));
    return roleRepresentation;
  }

  private void mapBasicAttributes(final CreatedServerRoleRepresentation representation, final ServerRoles roles) {
    roles.setPriority(representation.priority());
    roles.setColor(representation.color());
    roles.setName(representation.name());
  }

  private void mapRisks(final CreatedServerRoleRepresentation representation, final ServerRoles roles) {
    representation.risks().forEach(risk -> newRisk(roles, risk));
  }

  public ServerRoleRepresentation mapToRepresentation(ServerRoles roles) {
    return new ServerRoleRepresentation(
        roles.getId(),
        roles.getName(),
        roles.getColor(),
        roles.getPriority(),
        roles.getServer(),
        riskRepository.getRisks(roles.getId())
                      .map(risk -> new RiskRepresentation(risk.getType(), risk.getEntity(), risk.getMode()))
                      .toList(),
        serverRolesRepository.getMembers(roles.getId())
    );
  }

  @Transactional
  public void addRoleToUser(final UUID serverRoleId, final List<UUID> users) {
    ServerRoles roles = getEntity(serverRoleId);
    users.stream()
         .map(user -> entityManager.find(UserRoleMembership.class, user))
         .filter(Objects::nonNull)
         .forEach(user -> {
           user.getServerRoles().add(roles);
           entityManager.persist(user);
         });
    Notification.of(new NotificationServerRole(mapToRepresentation(roles), NotificationActionType.MODIFY)).sendTo(serverFinder.findUserForServer(roles.getServer()));
  }

  @Transactional
  public void removeUserToRole(final UUID serverRoleId, final List<UUID> users) {
    ServerRoles roles = getEntity(serverRoleId);
    users.stream()
         .map(user -> entityManager.find(UserRoleMembership.class, user))
         .filter(Objects::nonNull)
         .forEach(user -> {
           user.getServerRoles().remove(roles);
           entityManager.persist(user);
         });
    var roleRepresentation = mapToRepresentation(roles);
    Notification.of(new NotificationServerRole(roleRepresentation, NotificationActionType.MODIFY)).sendTo(serverFinder.findUserForServer(roles.getServer()));
  }

  @Transactional
  public void addRiskOrReplace(final UUID roleId, final RiskType type, final RiskMode mode) {
    ServerRoles roles = getEntity(roleId);
    riskRepository.getRisks(roleId).filter(risk -> risk.getType().equals(type))
                  .findFirst()
                  .ifPresentOrElse(risk -> updateMode(risk, mode),
                                   () -> newRisk(roles, new RiskRepresentation(type, null, mode))
                  );
    var roleRepresentation = mapToRepresentation(roles);
    Notification.of(new NotificationServerRole(roleRepresentation, NotificationActionType.MODIFY)).sendTo(serverFinder.findUserForServer(roles.getServer()));
  }

  private ServerRoles getEntity(final UUID serverRoleId) {
    ServerRoles roles = entityManager.find(ServerRoles.class, serverRoleId);
    if (roles == null) {
      throw new ResourceNotFoundException(ServerRoles.class, serverRoleId);
    }
    return roles;
  }

  private void updateMode(Risk risk, RiskMode mode) {
    risk.setMode(mode);
    entityManager.persist(risk);
  }

  private void newRisk(final ServerRoles roles, final RiskRepresentation risk) {
    var newRisk = new Risk();
    newRisk.setId(UUID.randomUUID());
    newRisk.setEntity(risk.entity());
    newRisk.setMode(risk.mode());
    newRisk.setType(risk.type());
    newRisk.setServerRoles(roles);
    entityManager.persist(newRisk);
  }
}
