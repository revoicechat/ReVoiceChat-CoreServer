package fr.revoicechat.core.error;

import java.util.UUID;

import fr.revoicechat.core.nls.CommonErrorCode;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception will automatically result in an HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(Class<?> clazz, UUID id) {
    super(CommonErrorCode.NOT_FOUND.translate(clazz.getSimpleName(), id));
  }
}
