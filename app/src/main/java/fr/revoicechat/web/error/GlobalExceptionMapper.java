package fr.revoicechat.web.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace(); // forces log in console
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("Server error: " + exception.getMessage())
                       .build();
    }
}
