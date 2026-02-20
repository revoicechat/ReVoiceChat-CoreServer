package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.*;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.user.AdminUpdatableUserData;
import fr.revoicechat.core.service.room.PrivateMessageService;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.representation.user.UpdatableUserData;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.core.web.api.UserController;

public class UserControllerImpl implements UserController {
  private final UserService userService;
  private final PrivateMessageService privateMessageService;

  public UserControllerImpl(final UserService userService, final PrivateMessageService privateMessageService) {
    this.userService = userService;
    this.privateMessageService = privateMessageService;
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public UserRepresentation me() {
    return userService.findCurrentUser();
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public UserRepresentation updateMe(final UpdatableUserData userData) {
    return userService.updateConnectedUser(userData);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public UserRepresentation get(UUID id) {
    return userService.get(id);
  }

  @Override
  public RoomRepresentation getPrivateMessage(final UUID id) {
    return privateMessageService.getDirectDiscussion(id);
  }

  @Override
  public MessageRepresentation sendPrivateMessage(final UUID id, final CreatedMessageRepresentation representation) {
    return privateMessageService.sendPrivateMessageTo(id, representation);
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public UserRepresentation updateAsAdmin(final UUID id, final AdminUpdatableUserData userData) {
    return userService.updateAsAdmin(id, userData);
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public List<UserRepresentation> fetchAll() {
    return userService.fetchAll();
  }
}
