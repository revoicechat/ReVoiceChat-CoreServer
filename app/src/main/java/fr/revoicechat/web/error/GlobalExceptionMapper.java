package fr.revoicechat.web.error;

import static fr.revoicechat.nls.HttpStatusErrorCode.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.nls.LocalizedMessage;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);

  private static final String JSON_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.json");
  private static final String HTML_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.html");

  private static String fetchForbiddenAccessFile(String name) {
    try (var ressource = GlobalExceptionMapper.class.getResourceAsStream(name)) {
      assert ressource != null;
      return new String(ressource.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @ConfigProperty(name = "revoicechat.error.log")
  boolean logError;
  @Context private HttpHeaders headers;

  @Override
  public Response toResponse(Throwable exception) {
    if (logError) {
      LOG.error(exception.getMessage(), exception);
    }
    return switch (exception) {
      case BadRequestException ex       -> toResponse(Status.BAD_REQUEST,           ex.getMessage());
      case ResourceNotFoundException ex -> toResponse(Status.NOT_FOUND,             ex.getMessage());
      case UnauthorizedException ignore -> toResponse(Status.UNAUTHORIZED,          UNAUTHORIZED_TITLE, UNAUTHORIZED_MESSAGE);
      case ForbiddenException ignore    -> toResponse(Status.FORBIDDEN,             FORBIDDEN_TITLE,    FORBIDDEN_MESSAGE);
      case NotFoundException ignore     -> toResponse(Status.NOT_FOUND,             NOT_FOUND_TITLE,    NOT_FOUND_MESSAGE);
      case NotAllowedException ignore   -> toResponse(Status.METHOD_NOT_ALLOWED,    NOT_FOUND_TITLE,    NOT_FOUND_MESSAGE);
      default                           -> toResponse(Status.INTERNAL_SERVER_ERROR, "Server error: " + exception.getMessage());
    };
  }

  private Response toResponse(Status status, String message) {
    return Response.status(status).entity(message).build();
  }

  private Response toResponse(Status status, LocalizedMessage title, LocalizedMessage message) {
    var type = determineResponseType();
    var entity = (type.equals(MediaType.TEXT_HTML_TYPE) ? HTML_MESSAGE : JSON_MESSAGE).formatted(title.translate(),
                                                                                                 message.translate(),
                                                                                                 DOCUMENTATION_API_LINK.translate());
    return Response.status(status)
                   .type(type)
                   .entity(entity)
                   .build();
  }

  private MediaType determineResponseType() {
    if (headers != null) {
      List<MediaType> acceptable = headers.getAcceptableMediaTypes();
      for (MediaType mediaType : acceptable) {
        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
          return MediaType.APPLICATION_JSON_TYPE;
        } else if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
          return MediaType.TEXT_HTML_TYPE;
        }
      }
    }
    return MediaType.TEXT_HTML_TYPE;
  }
}
