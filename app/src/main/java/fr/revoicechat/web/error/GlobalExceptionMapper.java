package fr.revoicechat.web.error;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.error.ResourceNotFoundException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);

  @ConfigProperty(name = "revoicechat.error.log")
  boolean logError;

  @Override
  public Response toResponse(Throwable exception) {
    if (logError) {
      LOG.error(exception.getMessage(), exception);
    }
    return switch (exception) {
      case BadRequestException ex -> toResponse(Status.BAD_REQUEST, ex.getMessage());
      case ResourceNotFoundException ex -> toResponse(Status.NOT_FOUND, ex.getMessage());
      case UnauthorizedException ignore -> toResponse(Status.UNAUTHORIZED, "Unauthorized");
      case ForbiddenException ignore -> toResponse(Status.FORBIDDEN, "Forbidden");
      default -> toResponse(Status.INTERNAL_SERVER_ERROR, "Server error");
    };
  }

  private Response toResponse(Status status, String message) {
    return Response.status(status).entity(message).build();
  }
}
