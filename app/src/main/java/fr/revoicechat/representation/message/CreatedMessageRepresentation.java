package fr.revoicechat.representation.message;

import java.util.List;

public record CreatedMessageRepresentation(
    String text,
    List<CreatedMediaDataRepresentation> medias) {
  public record CreatedMediaDataRepresentation(String name) {}
}
