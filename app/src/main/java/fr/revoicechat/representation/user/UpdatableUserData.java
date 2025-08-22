package fr.revoicechat.representation.user;

import fr.revoicechat.model.ActiveStatus;

public record UpdatableUserData(
    String displayName,
    String password,
    String newPassword,
    String confirmPassword,
    ActiveStatus status
) {}
