package fr.revoicechat.security;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String path = requestContext.getUriInfo().getPath();
    if (path.startsWith("auth/")) {
      return; // allow login/logout
    }

    HttpServletRequest req = (HttpServletRequest) requestContext.getProperty(HttpServletRequest.class.getName());
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("username") == null) {
      requestContext.abortWith(Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("Not logged in").build());
    }
  }
}
