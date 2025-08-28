package fr.revoicechat.core.model;

import static fr.revoicechat.core.security.utils.RevoiceChatRoles.*;

import java.util.Set;

public enum UserType {
  ADMIN(ROLE_USER, ROLE_ADMIN),
  USER(ROLE_USER),
  BOT(ROLE_USER),
  ;

  private final Set<String> roles;

  UserType(String... roles) {
    this.roles = Set.of(roles);
  }

  public Set<String> getRoles() {
    return roles;
  }
}
