package fr.revoicechat.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.service.UserService;
import fr.revoicechat.web.api.UserController;

@RestController
public class UserControllerImpl implements UserController {
  private final UserService userService;

  public UserControllerImpl(final UserService userService) {this.userService = userService;}

  @Override
  public UserRepresentation me() {
    return userService.findCurrentUser();
  }

  @Override
  public UserRepresentation get(final UUID id) {
    return userService.get(id);
  }
}
