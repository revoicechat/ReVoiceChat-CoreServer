package fr.revoicechat.risk.service;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.risk.representation.RiskCategoryRepresentation;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskType;

public interface RiskService {

  boolean hasRisk(UUID userId, RiskEntity entity, RiskType riskType);

  boolean hasRisk(RiskEntity entity, RiskType riskType);

  List<RiskCategoryRepresentation> getAllRisks();
}
