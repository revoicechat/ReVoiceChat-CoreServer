package fr.revoicechat.core.service.message;

import static fr.revoicechat.core.nls.MessageErrorCode.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.revoicechat.core.representation.media.CreatedMediaDataRepresentation;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.web.error.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;

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
