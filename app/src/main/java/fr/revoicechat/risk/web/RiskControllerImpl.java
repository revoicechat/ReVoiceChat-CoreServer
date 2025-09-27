package fr.revoicechat.risk.web;

import java.util.List;

import fr.revoicechat.risk.representation.RiskCategoryRepresentation;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.web.api.RiskController;
import jakarta.annotation.security.PermitAll;

public class RiskControllerImpl implements RiskController {

  private final RiskService riskService;

  public RiskControllerImpl(final RiskService riskService) {
    this.riskService = riskService;
  }

  @Override
  @PermitAll
  public List<RiskCategoryRepresentation> getAllRisks() {
    return riskService.getAllRisks();
  }
}
