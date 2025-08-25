package fr.revoicechat.web;

import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.representation.user.UpdatableUserData;
import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.service.UserService;
import fr.revoicechat.web.api.UserController;

@RolesAllowed("USER") // only authenticated users
public class UserControllerImpl implements UserController {
  private final UserService userService;

  public UserControllerImpl(final UserService userService) {this.userService = userService;}

  @Override
  public UserRepresentation me() {
    return userService.findCurrentUser();
  }

  @Override
  public UserRepresentation me(final UpdatableUserData userData) {
    return userService.updateConnectedUser(userData);
  }

  @Override
  public UserRepresentation get(UUID id) {
    return userService.get(id);
  }
}
