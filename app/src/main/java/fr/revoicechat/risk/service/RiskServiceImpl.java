package fr.revoicechat.risk.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.revoicechat.i18n.TranslationUtils;
import fr.revoicechat.risk.model.RiskMode;
import fr.revoicechat.risk.model.ServerRoles;
import fr.revoicechat.risk.repository.ServerRolesRepository;
import fr.revoicechat.risk.repository.ServerRolesRepository.AffectedRisk;
import fr.revoicechat.risk.representation.RiskCategoryRepresentation;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.RiskCategory;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.security.UserHolder;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

@Singleton
@Unremovable
class RiskServiceImpl implements RiskService {

  private final ServerRolesRepository serverRolesRepository;
  private final UserHolder userHolder;

  public RiskServiceImpl(final ServerRolesRepository serverRolesRepository, final UserHolder userHolder) {
    this.serverRolesRepository = serverRolesRepository;
    this.userHolder = userHolder;
  }

  @Override
  public boolean hasRisk(final RiskEntity entity, final RiskType riskType) {
    return hasRisk(userHolder.get().getId(), entity, riskType);
  }

  @Override
  public boolean hasRisk(final UUID userId, final RiskEntity entity, final RiskType riskType) {
    if (serverRolesRepository.isOwner(entity.serverId(), userId)) {
      return true;
    }
    List<ServerRoles> membership = serverRolesRepository.getServerRoles(userId);
    if (membership.isEmpty()) {
      return false;
    }
    var serverAffectedRisk = serverRolesRepository.getAffectedRisks(entity, riskType);
    return serverAffectedRisk.stream()
                             .filter(affectedRisk -> isUserMembership(affectedRisk, membership))
                             .filter(affectedRisk -> !affectedRisk.mode().equals(RiskMode.DEFAULT))
                             .findFirst()
                             .map(AffectedRisk::mode)
                             .orElse(RiskMode.DISABLE)
                             .isEnable();
  }

  private boolean isUserMembership(final AffectedRisk affectedRisk, final List<ServerRoles> membership) {
    return membership.stream().anyMatch(role -> role.getId().equals(affectedRisk.role()));
  }

  @Override
  public List<RiskCategoryRepresentation> getAllRisks() {
    return RiskTypeClassesHolder.INSTANCE.getRiskType()
                                         .stream()
                                         .filter(Class::isEnum)
                                         .filter(clazz -> clazz.isAnnotationPresent(RiskCategory.class))
                                         .map(clazz -> new RiskData(clazz.getEnumConstants(), clazz.getAnnotation(RiskCategory.class)))
                                         .filter(data -> data.riskTypes.length != 0)
                                         .map(this::mapToRiskCategory)
                                         .toList();
  }

  private RiskCategoryRepresentation mapToRiskCategory(RiskData data) {
    var risk = data.riskTypes[0];
    return new RiskCategoryRepresentation(
        data.category.value(),
        TranslationUtils.translate(risk.fileName(), data.category.value()),
        Set.of(data.riskTypes)
    );
  }

  private static final class RiskData {
    private final RiskType[] riskTypes;
    private final RiskCategory category;

    private RiskData(RiskType[] riskTypes, RiskCategory category) {
      this.riskTypes = riskTypes;
      this.category = category;
    }
  }
}
