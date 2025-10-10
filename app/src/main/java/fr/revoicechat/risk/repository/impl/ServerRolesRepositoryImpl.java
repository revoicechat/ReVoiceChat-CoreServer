package fr.revoicechat.risk.repository.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.repository.ServerRolesRepository;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ServerRolesRepositoryImpl implements ServerRolesRepository {

  private static final String SERVER_ID = "serverId";
  private static final String USER_ID = "userId";
  private static final String RISK_TYPE = "riskType";
  private static final String ENTITY_ID = "entityId";

  @PersistenceContext EntityManager entityManager;

  @Override
  public List<ServerRoles> getServerRoles(final UUID userId) {
    return entityManager.createQuery("""
                            select u.serverRoles
                            from UserRoleMembership u
                            where u.id = :userId""", ServerRoles.class)
                        .setParameter(USER_ID, userId)
                        .getResultList();
  }

  @Override
  public List<AffectedRisk> getAffectedRisks(final RiskEntity entity, final RiskType riskType) {
    return entityManager.createQuery("""
                            select sr.id, r.mode
                            from Risk r
                            join r.serverRoles sr
                            where r.type = :riskType
                              and sr.server = :serverId
                              and (r.entity is null or r.entity = :entityId)
                            order by sr.priority
                            """, AffectedRisk.class)
                        .setParameter(RISK_TYPE, riskType)
                        .setParameter(SERVER_ID, entity.serverId())
                        .setParameter(ENTITY_ID, entity.entityId())
                        .getResultList();
  }

  @Override
  public Stream<ServerRoles> getByServer(final UUID serverId) {
    return entityManager.createQuery("select r from ServerRoles r where r.server = :serverId", ServerRoles.class)
                        .setParameter(SERVER_ID, serverId)
                        .getResultStream();
  }

  @Override
  public boolean isOwner(UUID server, UUID user) {
    return !entityManager.createQuery("""
                             select 1
                             from Server s
                             where s.id = :serverId
                               and s.owner.id = :userId""", int.class)
                         .setParameter(SERVER_ID, server)
                         .setParameter(USER_ID, user)
                         .getResultList()
                         .isEmpty();
  }

  @Override
  public List<UUID> getMembers(final UUID server) {
    return entityManager.createQuery("""
                            select m.id
                            from UserRoleMembership m
                            join m.serverRoles sr
                            where sr.id = :serverId""", UUID.class)
                        .setParameter(SERVER_ID, server)
                        .getResultList();
  }
}
