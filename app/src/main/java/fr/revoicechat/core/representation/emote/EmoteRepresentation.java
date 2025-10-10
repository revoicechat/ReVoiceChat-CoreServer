package fr.revoicechat.core.representation.emote;

import java.util.List;
import java.util.UUID;

public record EmoteRepresentation(UUID id, String name, List<String> keywords) {}
