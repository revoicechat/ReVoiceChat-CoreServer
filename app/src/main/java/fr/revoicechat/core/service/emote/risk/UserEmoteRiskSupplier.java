package fr.revoicechat.core.service.emote.risk;

import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.security.model.AuthenticatedUser;

@ApplicationScoped
public class UserEmoteRiskSupplier implements EmoteRiskSupplier {
  @Override
  public boolean hasRisk(final Emote emote, final AuthenticatedUser user, final RiskType riskType) {
    return emote.getEntity() != null && emote.getEntity().equals(user.getId());
  }
}
