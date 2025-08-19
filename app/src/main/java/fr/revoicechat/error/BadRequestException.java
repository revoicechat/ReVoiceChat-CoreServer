package fr.revoicechat.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.revoicechat.nls.LocalizedMessage;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception will automatically result in an HTTP 404 Not Found response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

  public BadRequestException(LocalizedMessage message, Object... args) {
    super(message.translate(args));
  }
}
