package fr.revoicechat.core.security;

import java.util.UUID;

import fr.revoicechat.core.model.User;
import fr.revoicechat.notification.NotificationRegistrableHolder;

public interface UserHolder extends NotificationRegistrableHolder {
  @Override
  User get();
  User get(String jwtToken);
  UUID peekId(String jwtToken);
}
