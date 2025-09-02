package fr.revoicechat.core.representation.invitation;

import java.util.UUID;

public record InvitationRepresentation(UUID id, fr.revoicechat.core.model.InvitationLinkStatus status, fr.revoicechat.core.model.InvitationType type) {
}
