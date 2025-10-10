package fr.revoicechat.core.representation.emote;

import java.util.List;

public record CreationEmoteRepresentation(String fileName, String content, List<String> keywords) {}
