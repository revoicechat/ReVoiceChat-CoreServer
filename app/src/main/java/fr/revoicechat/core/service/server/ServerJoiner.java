package fr.revoicechat.core.service.server;

import static fr.revoicechat.core.model.InvitationLinkStatus.CREATED;
import static fr.revoicechat.core.model.InvitationType.SERVER_JOIN;
import static fr.revoicechat.core.nls.ServerErrorCode.*;

import java.util.UUID;

import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.service.invitation.InvitationLinkService;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Service responsible for rejoining a {@link Server}.
 */
@ApplicationScoped
public class ServerJoiner {

  private final InvitationLinkService invitationLinkService;
  private final ServerUserService serverUserService;
  private final ServerEntityService serverEntityService;
  private final UserHolder userHolder;

  public ServerJoiner(final InvitationLinkService invitationLinkService, final ServerUserService serverUserService, final ServerEntityService serverEntityService, final UserHolder userHolder) {
    this.invitationLinkService = invitationLinkService;
    this.serverUserService = serverUserService;
    this.serverEntityService = serverEntityService;
    this.userHolder = userHolder;
  }

  @Transactional
  public void joinPublic(final UUID serverId) {
    var server = serverEntityService.getEntity(serverId);
    if (!server.isPublic()) {
      throw new BadRequestException(SERVER_NOT_PUBLIC);
    }
    join(server);
  }

  @Transactional
  public void joinPrivate(final UUID invitation) {
    var invitationLink = invitationLinkService.getEntity(invitation);
    if (!isValideInvitation(invitationLink)) {
      throw new BadRequestException(NO_VALID_INVITATION);
    }
    join(invitationLink.getTargetedServer());
  }

  private void join(final Server server) {
    User user = userHolder.get();
    serverUserService.join(server, user);
  }

  private static boolean isValideInvitation(InvitationLink invitationLink) {
    return invitationLink != null
           && SERVER_JOIN.equals(invitationLink.getType())
           && CREATED.equals(invitationLink.getStatus());
  }
}
