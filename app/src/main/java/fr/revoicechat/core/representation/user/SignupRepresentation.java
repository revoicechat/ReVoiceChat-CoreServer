package fr.revoicechat.core.representation.user;

import java.util.UUID;

public record SignupRepresentation(
    String username,
    String password,
    String email,
    UUID invitationLink
) {}
