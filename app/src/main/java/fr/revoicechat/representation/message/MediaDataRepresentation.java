package fr.revoicechat.representation.message;

import java.util.UUID;

import fr.revoicechat.model.FileType;
import fr.revoicechat.model.MediaOrigin;

public record MediaDataRepresentation(
    UUID id,
    String name,
    String url,
    MediaOrigin origin,
    FileType type) {}
