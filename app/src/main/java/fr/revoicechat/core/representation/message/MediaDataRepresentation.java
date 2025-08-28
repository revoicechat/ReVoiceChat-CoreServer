package fr.revoicechat.core.representation.message;

import java.util.UUID;

import fr.revoicechat.core.model.FileType;
import fr.revoicechat.core.model.MediaOrigin;

public record MediaDataRepresentation(
    UUID id,
    String name,
    String url,
    MediaOrigin origin,
    FileType type) {}
