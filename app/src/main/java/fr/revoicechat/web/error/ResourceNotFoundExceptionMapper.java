package fr.revoicechat.web.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import fr.revoicechat.error.ResourceNotFoundException;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

  @Override
  public Response toResponse(ResourceNotFoundException ex) {
    return Response.status(Status.NOT_FOUND)
                   .entity(ex.getMessage())
                   .build();
  }
}
