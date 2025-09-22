package fr.revoicechat.security;

import java.util.UUID;

import fr.revoicechat.security.model.AuthenticatedUser;

public interface UserHolder {
  <T extends AuthenticatedUser> T get();
  UUID getId();
  AuthenticatedUser get(String jwtToken);
  UUID peekId(String jwtToken);
}
