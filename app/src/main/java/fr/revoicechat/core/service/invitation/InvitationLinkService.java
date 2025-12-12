package fr.revoicechat.core.service.invitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.config.SeverAppMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.web.error.ResourceNotFoundException;
import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.InvitationLinkRepository;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.security.UserHolder;

@ApplicationScoped
public class InvitationLinkService {

  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final ServerService serverService;
  private final InvitationLinkRepository invitationLinkRepository;
  private final SeverAppMode severAppMode;

  public InvitationLinkService(final UserHolder userHolder,
                               final EntityManager entityManager,
                               final ServerService serverService,
                               final InvitationLinkRepository invitationLinkRepository,
                               final SeverAppMode severAppMode) {
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.serverService = serverService;
    this.invitationLinkRepository = invitationLinkRepository;
    this.severAppMode = severAppMode;
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
    return new InvitationRepresentation(invitation.getId(), invitation.getStatus(), invitation.getType(), null);
  }

  @Transactional
  public InvitationRepresentation generateServerInvitation(final UUID serverId) {
    if (severAppMode.isMonoMode()) {
      return generateApplicationInvitation();
    }
    var server = serverService.getEntity(serverId);
    User user = userHolder.get();
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(InvitationLinkStatus.CREATED);
    invitation.setType(InvitationType.SERVER_JOIN);
    invitation.setTargetedServer(server);
    invitation.setSender(user);
    entityManager.persist(invitation);
    return new InvitationRepresentation(invitation.getId(), invitation.getStatus(), invitation.getType(), serverId);
  }

  public InvitationRepresentation get(final UUID id) {
    var link = entityManager.find(InvitationLink.class, id);
    if (link == null) {
      throw new ResourceNotFoundException(InvitationLink.class, id);
    }
    return toRepresentation(link);
  }

  @Transactional
  public void revoke(final UUID id) {
    var link = entityManager.find(InvitationLink.class, id);
    if (link != null) {
      link.setStatus(InvitationLinkStatus.REVOKED);
      entityManager.persist(link);
    }
  }

  public List<InvitationRepresentation> getAllServerInvitations(final UUID id) {
    if (severAppMode.isMonoMode()) {
      return getAllApplicationInvitations();
    }
    return invitationLinkRepository.getAllFromServer(id)
                                   .map(this::toRepresentation)
                                   .toList();
  }

  public List<InvitationRepresentation> getAllFromUser() {
    return invitationLinkRepository.getAllFromUser(userHolder.get().getId())
                                   .map(this::toRepresentation)
                                   .toList();
  }

  public List<InvitationRepresentation> getAllApplicationInvitations() {
    return invitationLinkRepository.allApplicationInvitations()
                                   .map(this::toRepresentation)
                                   .toList();
  }

  private InvitationRepresentation toRepresentation(final InvitationLink link) {
    return new InvitationRepresentation(link.getId(),
                                        link.getStatus(),
                                        link.getType(),
                                        Optional.ofNullable(link.getTargetedServer()).map(Server::getId).orElse(null));
  }
}
