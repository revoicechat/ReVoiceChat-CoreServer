package fr.revoicechat.risk.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.risk.model.RiskMode;
import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;

public interface ServerRolesRepository {

  List<ServerRoles> getServerRoles(final UUID userId);

  List<ServerRoles> getDefaultServerRoles(final UUID id);

  List<AffectedRisk> getAffectedRisks(RiskEntity entity, RiskType riskType);

  Stream<ServerRoles> getByServer(UUID serverId);

  boolean isOwner(UUID server, UUID user);

  List<UUID> getMembers(UUID server);

  record AffectedRisk(UUID role, RiskMode mode) {}
}
