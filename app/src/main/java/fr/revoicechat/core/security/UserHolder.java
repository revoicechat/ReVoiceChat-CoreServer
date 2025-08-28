package fr.revoicechat.core.security;

import fr.revoicechat.core.model.User;

public interface UserHolder {
  User get();
  User get(String jwtToken);
}
