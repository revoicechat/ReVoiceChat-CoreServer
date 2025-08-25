package fr.revoicechat.security;

import fr.revoicechat.model.User;

public interface UserHolder {
  User get();
  User get(String jwtToken);
}
