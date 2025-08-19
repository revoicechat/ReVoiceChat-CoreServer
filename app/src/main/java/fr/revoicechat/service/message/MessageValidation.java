package fr.revoicechat.service.message;

import static fr.revoicechat.nls.MessageErrorCode.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;

@Component
public class MessageValidation {

  private final int messageSize;

  public MessageValidation(@Value("${revoicechat.message.max-length:2000}") int messageSize) {
    this.messageSize = messageSize;
  }

  public void isValid(final CreatedMessageRepresentation creation) {
    if (creation.text().isBlank() && creation.medias().isEmpty()) {
      throw new BadRequestException(MESSAGE_CANNOT_BE_EMPTY);
    }
    if (creation.text().length() > messageSize) {
      throw new BadRequestException(MESSAGE_TOO_LONG, messageSize);
    }
    if (creation.medias().stream().map(CreatedMediaDataRepresentation::name).anyMatch(StringUtils::isBlank)) {
      throw new BadRequestException(MEDIA_DATA_SHOULD_HAVE_A_NAME);
    }
  }
}
