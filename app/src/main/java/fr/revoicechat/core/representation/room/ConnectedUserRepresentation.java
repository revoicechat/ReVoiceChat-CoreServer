package fr.revoicechat.core.representation.room;

import java.util.List;

import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.live.stream.representation.StreamRepresentation;

public record ConnectedUserRepresentation(UserRepresentation user,
                                          List<StreamRepresentation> streams) {}
