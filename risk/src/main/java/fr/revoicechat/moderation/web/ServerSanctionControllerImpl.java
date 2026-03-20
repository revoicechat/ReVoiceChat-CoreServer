package fr.revoicechat.moderation.web;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.moderation.model.Sanction;
import fr.revoicechat.moderation.representation.NewSanction;
import fr.revoicechat.moderation.representation.SanctionFilterParams;
import fr.revoicechat.moderation.representation.SanctionRepresentation;
import fr.revoicechat.moderation.service.SanctionCreator;
import fr.revoicechat.moderation.service.SanctionEntityService;
import fr.revoicechat.moderation.web.api.ServerSanctionController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import fr.revoicechat.security.utils.RevoiceChatRoles;
import fr.revoicechat.web.error.ResourceNotFoundException;
import fr.revoicechat.web.mapper.Mapper;

@RolesAllowed(RevoiceChatRoles.ROLE_USER)
public class ServerSanctionControllerImpl implements ServerSanctionController {
  private final SanctionEntityService sanctionEntityService;
  private final SanctionCreator sanctionCreator;

  public ServerSanctionControllerImpl(SanctionEntityService sanctionEntityService, SanctionCreator sanctionCreator) {
    this.sanctionEntityService = sanctionEntityService;
    this.sanctionCreator = sanctionCreator;
  }

  @Override
  public List<SanctionRepresentation> getSanctions(final UUID serverId, final SanctionFilterParams params) {
    return Mapper.mapAll(sanctionEntityService.getAll(serverId, params));
  }

  @Override
  public SanctionRepresentation getSanction(final UUID serverId, final UUID id) {
    var sanction = sanctionEntityService.get(id);
    if (!Objects.equals(sanction.getServer(), serverId)) {
      throw new ResourceNotFoundException(Sanction.class, id);
    }
    return Mapper.map(sanction);
  }

  @Override
  @RisksMembershipData(risks = "TOGGLE_SANCTION", retriever = ServerIdRetriever.class)
  public SanctionRepresentation issueServerLevelSanction(final UUID serverId, final NewSanction newSanction) {
    return Mapper.map(sanctionCreator.create(serverId, newSanction));
  }

  @Override
  @RisksMembershipData(risks = "REVOKE_SANCTION", retriever = ServerIdRetriever.class)
  public void revokeServerLevelSanction(final UUID serverId, final UUID id) {
    sanctionCreator.revoke(serverId, id);
  }
}
