package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.*;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.user.AdminUpdatableUserData;
import fr.revoicechat.core.service.room.PrivateMessageService;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.web.mapper.Mapper;
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
    return Mapper.map(userService.findCurrentUser());
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public UserRepresentation updateMe(final UpdatableUserData userData) {
    UserRepresentation representation = Mapper.map(userService.updateConnectedUser(userData));
    Notification.of(representation).sendTo(userService.everyone());
    return representation;
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public UserRepresentation get(UUID id) {
    return Mapper.map(userService.getUser(id));
  }

  @Override
  public RoomRepresentation getPrivateMessage(final UUID id) {
    return Mapper.map(privateMessageService.getDirectDiscussion(id));
  }

  @Override
  public MessageRepresentation sendPrivateMessage(final UUID id, final CreatedMessageRepresentation representation) {
    return Mapper.map(privateMessageService.sendPrivateMessageTo(id, representation));
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public UserRepresentation updateAsAdmin(final UUID id, final AdminUpdatableUserData userData) {
    UserRepresentation representation = Mapper.map(userService.updateAsAdmin(id, userData));
    Notification.of(representation).sendTo(userService.everyone());
    return representation;
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public List<UserRepresentation> fetchAll() {
    return Mapper.mapAll(userService.fetchAll());
  }
}
