package fr.revoicechat.core.service.invitation;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.security.UserHolder;

@ApplicationScoped
public class InvitationLinkService {

  private final UserHolder userHolder;
  private final EntityManager entityManager;

  public InvitationLinkService(final UserHolder userHolder, final EntityManager entityManager) {
    this.userHolder = userHolder;
    this.entityManager = entityManager;
  }

  @Transactional
  public InvitationRepresentation generateApplicationInvitation() {
    User user = userHolder.get();
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(InvitationLinkStatus.CREATED);
    invitation.setType(InvitationType.APPLICATION_JOIN);
    invitation.setSender(user);
    entityManager.persist(invitation);
    return new InvitationRepresentation(invitation.getId(), invitation.getStatus(), invitation.getType());
  }

  public InvitationRepresentation get(final UUID id) {
    var link = entityManager.find(InvitationLink.class, id);
    if (link == null) {
      throw new ResourceNotFoundException(InvitationLink.class, id);
    }
    return new InvitationRepresentation(link.getId(), link.getStatus(), link.getType());
  }

  @Transactional
  public void revoke(final UUID id) {
    var link = entityManager.find(InvitationLink.class, id);
    if (link != null) {
      link.setStatus(InvitationLinkStatus.REVOKED);
      entityManager.persist(link);
    }
  }
}
