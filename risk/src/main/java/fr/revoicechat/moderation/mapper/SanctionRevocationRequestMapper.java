package fr.revoicechat.moderation.mapper;

import fr.revoicechat.moderation.model.SanctionRevocationRequest;
import fr.revoicechat.moderation.representation.SanctionSanctionRevocationRequestRepresentation;
import fr.revoicechat.web.mapper.RepresentationMapper;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

@Unremovable
@ApplicationScoped
public class SanctionRevocationRequestMapper implements RepresentationMapper<SanctionRevocationRequest, SanctionSanctionRevocationRequestRepresentation> {
  @Override
  public SanctionSanctionRevocationRequestRepresentation map(final SanctionRevocationRequest request) {
    return new SanctionSanctionRevocationRequestRepresentation(
        request.getId(),
        request.getSanction().getId(),
        request.getMessage(),
        request.getStatus(),
        request.getRequestAt(),
        request.canRequestAgain()
    );
  }
}
