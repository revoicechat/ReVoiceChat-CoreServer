package fr.revoicechat.risk.service;

import java.util.UUID;

import fr.revoicechat.moderation.service.SanctionService;
import fr.revoicechat.risk.model.RiskMode;
import fr.revoicechat.risk.technicaldata.AffectedRisk;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.security.UserHolder;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

@Singleton
@Unremovable
public class RiskServiceImpl implements RiskService {

  private final AffectedRiskService affectedRiskService;
  private final UserHolder userHolder;

  public RiskServiceImpl(final AffectedRiskService affectedRiskService, final UserHolder userHolder) {
    this.affectedRiskService = affectedRiskService;
    this.userHolder = userHolder;
  }

  @Override
  public boolean hasRisk(final RiskEntity entity, final RiskType riskType) {
    return hasRisk(userHolder.get().getId(), entity, riskType);
  }

  @Override
  public boolean hasRisk(final UUID userId, final RiskEntity entity, final RiskType riskType) {
    return affectedRiskService.get(userId, entity, riskType)
                              .map(AffectedRisk::mode)
                              .orElse(RiskMode.DISABLE)
                              .isEnable();
  }
}
