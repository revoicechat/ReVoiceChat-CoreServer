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

  @PersistenceContext EntityManager entityManager;

  @Override
  public List<ServerRoles> getServerRoles(final UUID userId) {
    return entityManager.createQuery("""
                            select u.serverRoles
                            from UserRoleMembership u
                            where u.id = :id""", ServerRoles.class)
                        .setParameter("id", userId)
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
                        .setParameter("riskType", riskType)
                        .setParameter("serverId", entity.serverId())
                        .setParameter("entityId", entity.entityId())
                        .getResultList();
  }

  @Override
  public Stream<ServerRoles> getByServer(final UUID serverId) {
    return entityManager.createQuery("select r from ServerRoles r where r.server = :serverId", ServerRoles.class)
                        .setParameter("serverId", serverId)
                        .getResultStream();
  }

  @Override
  public boolean isOwner(UUID server, UUID user) {
    return !entityManager.createQuery("""
                             select 1
                             from Server s
                             where s.id = :server
                               and s.owner.id = :user""", int.class)
                         .setParameter("server", server)
                         .setParameter("user", user)
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
                        .setParameter("serverId", server)
                        .getResultList();
  }
}
