package fr.revoicechat.core.risk;

import fr.revoicechat.risk.type.RiskCategory;
import fr.revoicechat.risk.type.RiskType;

@RiskCategory("INVITATION_RISK_TYPE")
public enum InvitationRiskType implements RiskType {
  SERVER_INVITATION_ADD,
  SERVER_INVITATION_FETCH,
}
