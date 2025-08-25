package fr.revoicechat.service.message;

import static fr.revoicechat.nls.MessageErrorCode.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;

class TestMessageValidation {

  @Test
  void testBlankMessageAndNoMedia() {
    CreatedMessageRepresentation creation = new CreatedMessageRepresentation("", List.of());
    var validation = new MessageValidation();
    validation.messageSize = 10;
    Assertions.assertThatThrownBy(() -> validation.isValid(creation))
              .isInstanceOf(BadRequestException.class)
              .hasMessage(MESSAGE_CANNOT_BE_EMPTY.translate());
  }

  @Test
  void testNoMediaAndTooLongMessage() {
    CreatedMessageRepresentation creation = new CreatedMessageRepresentation("0123456789+", List.of());
    var validation = new MessageValidation();
    validation.messageSize = 10;
    Assertions.assertThatThrownBy(() -> validation.isValid(creation))
              .isInstanceOf(BadRequestException.class)
              .hasMessage(MESSAGE_TOO_LONG.translate(10));
  }

  @Test
  void testNoMediaAndMaxSizeMessage() {
    CreatedMessageRepresentation creation = new CreatedMessageRepresentation("0123456789", List.of());
    var validation = new MessageValidation();
    validation.messageSize = 10;
    Assertions.assertThatCode(() -> validation.isValid(creation)).doesNotThrowAnyException();
  }

  @Test
  void testNoMessageAndMediaWithoutName() {
    CreatedMessageRepresentation creation = new CreatedMessageRepresentation(
        "",
        List.of(new CreatedMediaDataRepresentation("test"),
                new CreatedMediaDataRepresentation(""))
    );
    var validation = new MessageValidation();
    validation.messageSize = 10;
    Assertions.assertThatThrownBy(() -> validation.isValid(creation))
              .isInstanceOf(BadRequestException.class)
              .hasMessage(MEDIA_DATA_SHOULD_HAVE_A_NAME.translate());
  }

  @Test
  void testNoError() {
    CreatedMessageRepresentation creation = new CreatedMessageRepresentation(
        "1234",
        List.of(new CreatedMediaDataRepresentation("test"), new CreatedMediaDataRepresentation("test2"))
    );
    var validation = new MessageValidation();
    validation.messageSize = 10;
    Assertions.assertThatCode(() -> validation.isValid(creation)).doesNotThrowAnyException();
  }
}