package fr.revoicechat.core.web;

import java.util.UUID;

import fr.revoicechat.core.representation.user.AdminUpdatableUserData;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.representation.user.UpdatableUserData;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.core.web.api.UserController;

public class UserControllerImpl implements UserController {
  private final UserService userService;

  public UserControllerImpl(final UserService userService) {this.userService = userService;}

  @Override
  @RolesAllowed("USER")
  public UserRepresentation me() {
    return userService.findCurrentUser();
  }

  @Override
  @RolesAllowed("USER")
  public UserRepresentation me(final UpdatableUserData userData) {
    return userService.updateConnectedUser(userData);
  }

  @Override
  @RolesAllowed("USER")
  public UserRepresentation get(UUID id) {
    return userService.get(id);
  }

  @Override
  @RolesAllowed("ADMIN")
  public UserRepresentation updateAsAdmin(final UUID id, final AdminUpdatableUserData userData) {
    return userService.updateAsAdmin(id, userData);
  }
}
