package fr.revoicechat.core.service.invitation;

import static fr.revoicechat.core.model.InvitationLinkStatus.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.InvitationLinkRepository;
import fr.revoicechat.core.representation.invitation.InvitationCategory;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.service.server.ServerEntityService;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InvitationLinkService implements InvitationLinkEntityRetriever, InvitationLinkUsage {

  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final ServerEntityService serverService;
  private final InvitationLinkRepository invitationLinkRepository;

  public InvitationLinkService(final UserHolder userHolder,
                               final EntityManager entityManager,
                               final ServerEntityService serverService,
                               final InvitationLinkRepository invitationLinkRepository) {
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.serverService = serverService;
    this.invitationLinkRepository = invitationLinkRepository;
  }

  @Transactional
  public InvitationRepresentation generateApplicationInvitation(final InvitationCategory invitationCategory) {
    User user = userHolder.get();
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(invitationCategory.getInitialStatus());
    invitation.setType(InvitationType.APPLICATION_JOIN);
    invitation.setSender(user);
    entityManager.persist(invitation);
    return new InvitationRepresentation(invitation.getId(), invitation.getStatus(), invitation.getType(), null);
  }

  @Transactional
  public InvitationRepresentation generateServerInvitation(final UUID serverId, final InvitationCategory invitationCategory) {
    var server = serverService.getEntity(serverId);
    User user = userHolder.get();
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(invitationCategory.getInitialStatus());
    invitation.setType(InvitationType.SERVER_JOIN);
    invitation.setTargetedServer(server);
    invitation.setSender(user);
    entityManager.persist(invitation);
    return new InvitationRepresentation(invitation.getId(), invitation.getStatus(), invitation.getType(), serverId);
  }

  @Override
  public InvitationLink getEntity(final UUID invitationId) {
    return Optional.ofNullable(invitationId)
                   .map(id -> entityManager.find(InvitationLink.class, id))
                   .orElse(null);
  }

  public InvitationRepresentation get(final UUID id) {
    var link = getEntity(id);
    if (link == null) {
      throw new ResourceNotFoundException(InvitationLink.class, id);
    }
    return toRepresentation(link);
  }

  @Transactional
  public void revoke(final UUID id) {
    var link = entityManager.find(InvitationLink.class, id);
    if (link != null) {
      link.setStatus(REVOKED);
      entityManager.persist(link);
    }
  }

  public List<InvitationRepresentation> getAllServerInvitations(final UUID id) {
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

  @Override
  @Transactional
  public void use(final InvitationLink invitationLink) {
    use(invitationLink, userHolder.get());
  }

  @Override
  @Transactional
  public void use(final InvitationLink invitationLink, final User user) {
    if (invitationLink != null && Objects.equals(invitationLink.getStatus(), CREATED)) {
      invitationLink.setStatus(USED);
      invitationLink.setApplier(user);
      entityManager.persist(invitationLink);
    }
  }

  private InvitationRepresentation toRepresentation(final InvitationLink link) {
    return new InvitationRepresentation(link.getId(),
                                        link.getStatus(),
                                        link.getType(),
                                        Optional.ofNullable(link.getTargetedServer()).map(Server::getId).orElse(null));
  }
}
