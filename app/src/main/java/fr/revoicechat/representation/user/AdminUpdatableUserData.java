package fr.revoicechat.representation.user;

import fr.revoicechat.model.UserType;

public record AdminUpdatableUserData(String displayName, UserType type) {}
