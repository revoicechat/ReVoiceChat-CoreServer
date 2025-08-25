package fr.revoicechat.web.error;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

  @Override
  public Response toResponse(BadRequestException ex) {
    return Response.status(Status.BAD_REQUEST)
                   .entity(ex.getMessage())
                   .build();
  }
}
