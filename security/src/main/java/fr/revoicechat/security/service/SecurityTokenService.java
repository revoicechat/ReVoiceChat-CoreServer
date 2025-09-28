package fr.revoicechat.security.service;

import java.util.UUID;

import fr.revoicechat.security.model.AuthenticatedUser;

public interface SecurityTokenService {
  String generate(final AuthenticatedUser user);
  UUID retrieveUserAsId(String jwtToken);
}
