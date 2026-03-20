package fr.revoicechat.moderation.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.moderation.model.Sanction;
import fr.revoicechat.moderation.representation.SanctionRepresentation;
import fr.revoicechat.web.mapper.RepresentationMapper;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class SanctionMapper implements RepresentationMapper<Sanction, SanctionRepresentation> {
  @Override
  public SanctionRepresentation map(final Sanction sanction) {
    return new SanctionRepresentation(
        sanction.getId(),
        sanction.getTargetedUser(),
        sanction.getServer(),
        sanction.getType(),
        sanction.getStartAt(),
        sanction.getExpiresAt(),
        sanction.getIssuedBy(),
        sanction.getReason(),
        sanction.getRevokedBy(),
        sanction.getRevokedAt(),
        sanction.isActive()
    );
  }
}
