package fr.revoicechat.risk.representation;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.revoicechat.risk.service.risk.TranslatedRiskTypeSerializer;
import fr.revoicechat.risk.type.RiskType;

public record RiskCategoryRepresentation(
    String type,
    String title,
    List<TranslatedRisk> risks
) {

  public RiskCategoryRepresentation(final String type, final String title, Collection<RiskType> risks) {
    this(type, title, risks.stream().map(TranslatedRisk::new).toList());
  }

  @JsonSerialize(using = TranslatedRiskTypeSerializer.class)
  private record TranslatedRisk(RiskType type) {}
}
