package fr.revoicechat.representation.message;

import java.util.List;
import java.util.Optional;

public record CreatedMessageRepresentation(
    String text,
    List<CreatedMediaDataRepresentation> medias) {

  public CreatedMessageRepresentation(final String text, final List<CreatedMediaDataRepresentation> medias) {
    this.text = Optional.ofNullable(text).map(String::trim).orElse("");
    this.medias = Optional.ofNullable(medias).orElse(List.of());
  }

  public record CreatedMediaDataRepresentation(String name) {}
}
