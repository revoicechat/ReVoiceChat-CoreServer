package fr.revoicechat.core.web.error;

import static fr.revoicechat.core.nls.HttpStatusErrorCode.*;
import static fr.revoicechat.core.web.error.ErrorMapperUtils.*;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.error.BadRequestException;
import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.nls.LocalizedMessage;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);

  @ConfigProperty(name = "revoicechat.error.log") boolean logError;
  @Context private HttpHeaders headers;

  private final UnknownErrorFileGenerator errorFileGenerator;

  public GlobalExceptionMapper(final UnknownErrorFileGenerator errorFileGenerator) {
    this.errorFileGenerator = errorFileGenerator;
  }

  @Override
  public Response toResponse(Throwable exception) {
    if (logError) {
      LOG.error(exception.getMessage(), exception);
    }
    return switch (exception) {
      case BadRequestException ex -> toResponse(Status.BAD_REQUEST, ex.getMessage());
      case ResourceNotFoundException ex -> toResponse(Status.NOT_FOUND, ex.getMessage());
      case UnauthorizedException ignore -> toResponse(Status.UNAUTHORIZED, UNAUTHORIZED_TITLE, UNAUTHORIZED_MESSAGE);
      case ForbiddenException ignore -> toResponse(Status.FORBIDDEN, FORBIDDEN_TITLE, FORBIDDEN_MESSAGE);
      case NotFoundException ignore -> toResponse(Status.NOT_FOUND, NOT_FOUND_TITLE, NOT_FOUND_MESSAGE);
      case NotAllowedException ignore -> toResponse(Status.METHOD_NOT_ALLOWED, NOT_FOUND_TITLE, NOT_FOUND_MESSAGE);
      default -> {
        String fileName = errorFileGenerator.generate(exception);
        var type = determineResponseType(headers);
        yield Response.status(INTERNAL_SERVER_ERROR).type(type.type()).entity(type.unknownErrorFile(fileName)).build();
      }
    };
  }

  private Response toResponse(Status status, String message) {
    return Response.status(status).entity(message).build();
  }

  private Response toResponse(Status status, LocalizedMessage title, LocalizedMessage message) {
    var type = determineResponseType(headers);
    return Response.status(status).type(type.type()).entity(type.genericErrorFile(title, message)).build();
  }
}
