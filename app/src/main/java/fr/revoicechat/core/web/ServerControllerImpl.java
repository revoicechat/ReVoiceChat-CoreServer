package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.core.service.invitation.InvitationLinkService;
import fr.revoicechat.core.web.api.ServerController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class ServerControllerImpl implements ServerController {

  private final ServerService serverService;
  private final RoomService roomService;
  private final UserService userService;
  private final InvitationLinkService invitationLinkService;

  public ServerControllerImpl(ServerService serverService, RoomService roomService, UserService userService, InvitationLinkService invitationLinkService) {
    this.serverService = serverService;
    this.roomService = roomService;
    this.userService = userService;
    this.invitationLinkService = invitationLinkService;
  }

  @Override
  public List<ServerRepresentation> getServers() {
    return serverService.getAll();
  }

  @Override
  public ServerRepresentation getServer(UUID id) {
    return serverService.get(id);
  }

  @Override
  public ServerRepresentation createServer(ServerCreationRepresentation representation) {
    return serverService.create(representation);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_UPDATE", retriever = ServerIdRetriever.class)
  public ServerRepresentation updateServer(UUID id, ServerCreationRepresentation representation) {
    return serverService.update(id, representation);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_DELETE", retriever = ServerIdRetriever.class)
  public void deleteServer(final UUID id) {
    serverService.delete(id);
  }

  @Override
  public List<RoomRepresentation> getRooms(UUID id) {
    return roomService.findAll(id);
  }

  @Override
  public ServerStructure getStructure(final UUID id) {
    return serverService.getStructure(id);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_UPDATE", retriever = ServerIdRetriever.class)
  public ServerStructure patchStructure(final UUID id, final ServerStructure structure) {
    return serverService.updateStructure(id, structure);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_ROOM_ADD", retriever = ServerIdRetriever.class)
  public RoomRepresentation createRoom(UUID id, CreationRoomRepresentation representation) {
    return roomService.create(id, representation);
  }

  @Override
  public List<UserRepresentation> fetchUsers(final UUID id) {
    return userService.fetchUserForServer(id);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_INVITATION_ADD", retriever = ServerIdRetriever.class)
  public InvitationRepresentation generateServerInvitation(final UUID id) {
    return invitationLinkService.generateServerInvitation(id);
  }

  @Override
  @RisksMembershipData(risks = "SERVER_INVITATION_FETCH", retriever = ServerIdRetriever.class)
  public List<InvitationRepresentation> getAllServerInvitations(final UUID id) {
    return invitationLinkService.getAllServerInvitations(id);
  }
}
