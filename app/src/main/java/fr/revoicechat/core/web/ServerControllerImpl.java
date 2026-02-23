package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.*;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.invitation.InvitationCategory;
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
import fr.revoicechat.core.service.server.ServerDeleterService;
import fr.revoicechat.core.service.server.ServerJoiner;
import fr.revoicechat.core.service.server.ServerRetriever;
import fr.revoicechat.core.web.api.ServerController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import fr.revoicechat.web.mapper.Mapper;
import jakarta.annotation.security.RolesAllowed;

public class ServerControllerImpl implements ServerController {

  private final ServerRetriever serverRetriever;
  private final ServerService serverService;
  private final RoomService roomService;
  private final UserService userService;
  private final InvitationLinkService invitationLinkService;
  private final ServerJoiner serverJoiner;
  private final ServerDeleterService serverDeleterService;

  public ServerControllerImpl(ServerRetriever serverRetriever, ServerService serverService, RoomService roomService, UserService userService, InvitationLinkService invitationLinkService, ServerJoiner serverJoiner, ServerDeleterService serverDeleterService) {
    this.serverRetriever = serverRetriever;
    this.serverService = serverService;
    this.roomService = roomService;
    this.userService = userService;
    this.invitationLinkService = invitationLinkService;
    this.serverJoiner = serverJoiner;
    this.serverDeleterService = serverDeleterService;
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<ServerRepresentation> getServers() {
    return Mapper.mapAll(serverRetriever.getAllMyServers());
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public List<ServerRepresentation> getAllServers() {
    return Mapper.mapAll(serverService.getAll());
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<ServerRepresentation> getPublicServers(boolean joinedToo) {
    return Mapper.mapAll(serverRetriever.getAllPublicServers(joinedToo));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public ServerRepresentation getServer(UUID id) {
    return Mapper.map(serverRetriever.getEntity(id));
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public ServerRepresentation createServer(ServerCreationRepresentation representation) {
    return Mapper.map(serverService.create(representation));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "SERVER_UPDATE", retriever = ServerIdRetriever.class)
  public ServerRepresentation updateServer(UUID id, ServerCreationRepresentation representation) {
    return Mapper.map(serverService.update(id, representation));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "SERVER_DELETE", retriever = ServerIdRetriever.class)
  public void deleteServer(final UUID id) {
    serverDeleterService.delete(id);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<RoomRepresentation> getRooms(UUID id) {
    return Mapper.mapAll(roomService.findAllForCurrentUser(id));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "SERVER_ROOM_ADD", retriever = ServerIdRetriever.class)
  public RoomRepresentation createRoom(UUID id, CreationRoomRepresentation representation) {
    return Mapper.map(roomService.create(id, representation));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<UserRepresentation> fetchUsers(final UUID id) {
    return Mapper.mapAll(userService.fetchUserForServer(id));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "SERVER_INVITATION_ADD", retriever = ServerIdRetriever.class)
  public InvitationRepresentation generateServerInvitation(final UUID id, final String category) {
    return Mapper.map(invitationLinkService.generateServerInvitation(id, InvitationCategory.of(category)));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "SERVER_INVITATION_FETCH", retriever = ServerIdRetriever.class)
  public List<InvitationRepresentation> getAllServerInvitations(final UUID id) {
    return Mapper.mapAll(invitationLinkService.getAllServerInvitations(id));
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public void joinPublic(final UUID serverId) {
    serverJoiner.joinPublic(serverId);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public void joinPrivate(final UUID invitation) {
    serverJoiner.joinPrivate(invitation);
  }
}
