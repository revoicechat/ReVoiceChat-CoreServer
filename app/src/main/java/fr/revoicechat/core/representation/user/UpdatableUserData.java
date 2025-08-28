package fr.revoicechat.core.representation.user;

import fr.revoicechat.core.model.ActiveStatus;

public record UpdatableUserData(
    String displayName,
    PasswordUpdated password,
    ActiveStatus status
) {
  public record PasswordUpdated(String password,
                                String newPassword,
                                String confirmPassword) {}
}
