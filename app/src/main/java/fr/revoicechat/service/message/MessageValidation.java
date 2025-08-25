package fr.revoicechat.service.message;

import static fr.revoicechat.nls.MessageErrorCode.*;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;

@ApplicationScoped
public class MessageValidation {

  @ConfigProperty(name = "revoicechat.message.max-length")
  int messageSize;

  public void isValid(final CreatedMessageRepresentation creation) {
    if (creation.text().isBlank() && creation.medias().isEmpty()) {
      throw new BadRequestException(MESSAGE_CANNOT_BE_EMPTY);
    }
    if (creation.text().length() > messageSize) {
      throw new BadRequestException(MESSAGE_TOO_LONG, messageSize);
    }
    if (creation.medias().stream().map(CreatedMediaDataRepresentation::name).anyMatch(String::isBlank)) {
      throw new BadRequestException(MEDIA_DATA_SHOULD_HAVE_A_NAME);
    }
  }
}
