package fr.revoicechat.moderation.web;

import java.util.List;
import java.util.UUID;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.moderation.model.Sanction;
import fr.revoicechat.moderation.representation.NewSanction;
import fr.revoicechat.moderation.representation.SanctionFilterParams;
import fr.revoicechat.moderation.representation.SanctionRepresentation;
import fr.revoicechat.moderation.service.SanctionCreator;
import fr.revoicechat.moderation.service.SanctionEntityService;
import fr.revoicechat.moderation.web.api.AppSanctionController;
import fr.revoicechat.security.utils.RevoiceChatRoles;
import fr.revoicechat.web.error.ResourceNotFoundException;
import fr.revoicechat.web.mapper.Mapper;

public class AppSanctionControllerImpl implements AppSanctionController {
  private final SanctionEntityService sanctionEntityService;
  private final SanctionCreator sanctionCreator;

  public AppSanctionControllerImpl(SanctionEntityService sanctionEntityService, SanctionCreator sanctionCreator) {
    this.sanctionEntityService = sanctionEntityService;
    this.sanctionCreator = sanctionCreator;
  }

  @Override
  @RolesAllowed(RevoiceChatRoles.ROLE_USER)
  public List<SanctionRepresentation> getSanctions(final SanctionFilterParams params) {
    return Mapper.mapAll(sanctionEntityService.getAll(null, params));
  }

  @Override
  @RolesAllowed(RevoiceChatRoles.ROLE_USER)
  public SanctionRepresentation getSanction(final UUID id) {
    var sanction = sanctionEntityService.get(id);
    if (sanction.getServer() != null) {
      throw new ResourceNotFoundException(Sanction.class, id);
    }
    return Mapper.map(sanction);
  }

  @Override
  @RolesAllowed(RevoiceChatRoles.ROLE_ADMIN)
  public SanctionRepresentation issueAppLevelSanction(final NewSanction newSanction) {
    return Mapper.map(sanctionCreator.create(null, newSanction));
  }

  @Override
  @RolesAllowed(RevoiceChatRoles.ROLE_ADMIN)
  public void revokeAppLevelSanction(final UUID id) {
    sanctionCreator.revoke(null, id);
  }
}
