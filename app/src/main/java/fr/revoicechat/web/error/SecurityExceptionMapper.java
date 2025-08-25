package fr.revoicechat.web.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception ex) {
    if (ex instanceof UnauthorizedException) {
      return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
    } else if (ex instanceof ForbiddenException) {
      return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
  }
}
