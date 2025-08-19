package fr.revoicechat.error;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.revoicechat.nls.CommonErrorCode;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception will automatically result in an HTTP 404 Not Found response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(Class<?> clazz, UUID id) {
    super(CommonErrorCode.NOT_FOUND.translate(clazz.getSimpleName(), id));
  }
}
