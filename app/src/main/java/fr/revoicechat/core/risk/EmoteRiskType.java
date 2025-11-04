package fr.revoicechat.core.risk;

import fr.revoicechat.risk.type.RiskCategory;
import fr.revoicechat.risk.type.RiskType;

@RiskCategory("EMOTE_RISK_TYPE")
public enum EmoteRiskType implements RiskType {
  ADD_EMOTE,
  UPDATE_EMOTE,
  REMOVE_EMOTE,
}
