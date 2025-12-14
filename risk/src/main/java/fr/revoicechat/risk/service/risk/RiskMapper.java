package fr.revoicechat.risk.service.risk;

import java.util.UUID;

import fr.revoicechat.risk.model.Risk;
import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.representation.RiskRepresentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RiskMapper {

  private final EntityManager entityManager;

  public RiskMapper(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Transactional
  public void map(final ServerRoles roles, final RiskRepresentation risk) {
    var newRisk = new Risk();
    newRisk.setId(UUID.randomUUID());
    newRisk.setEntity(risk.entity());
    newRisk.setMode(risk.mode());
    newRisk.setType(risk.type());
    newRisk.setServerRoles(roles);
    entityManager.persist(newRisk);
  }
}
