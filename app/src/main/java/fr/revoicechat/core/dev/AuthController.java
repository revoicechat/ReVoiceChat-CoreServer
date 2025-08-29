package fr.revoicechat.core.dev;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/error/throw")
@IfBuildProfile("dev")
public class AuthController {

  @GET
  @PermitAll
  public String error() {
    throw new UnsupportedOperationException("this method is here to test error log generation");
  }
}
