package fr.revoicechat.core.representation.user;

import fr.revoicechat.core.model.UserType;

public record AdminUpdatableUserData(String displayName, UserType type) {}
