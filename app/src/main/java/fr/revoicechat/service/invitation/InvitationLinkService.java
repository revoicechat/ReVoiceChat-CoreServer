package fr.revoicechat.service.invitation;

import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.revoicechat.model.InvitationLink;
import fr.revoicechat.model.InvitationLinkStatus;
import fr.revoicechat.model.InvitationType;
import fr.revoicechat.representation.invitation.InvitationRepresentation;
import fr.revoicechat.security.UserHolder;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class InvitationLinkService {

  private final UserHolder userHolder;
  private final EntityManager entityManager;

  public InvitationLinkService(final UserHolder userHolder, final EntityManager entityManager) {
    this.userHolder = userHolder;
    this.entityManager = entityManager;
  }

  @Transactional
  public InvitationRepresentation generateApplicationInvitation() {
    var user = userHolder.get();
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(InvitationLinkStatus.CREATED);
    invitation.setType(InvitationType.APPLICATION_JOIN);
    invitation.setSender(user);
    entityManager.persist(invitation);
    return new InvitationRepresentation(invitation.getId());
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
