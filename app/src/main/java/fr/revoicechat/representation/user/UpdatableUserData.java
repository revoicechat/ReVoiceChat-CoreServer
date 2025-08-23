package fr.revoicechat.representation.user;

import fr.revoicechat.model.ActiveStatus;

public record UpdatableUserData(
    String displayName,
    PasswordUpdated password,
    ActiveStatus status
) {
  public record PasswordUpdated(String password,
                                String newPassword,
                                String confirmPassword) {}
}
