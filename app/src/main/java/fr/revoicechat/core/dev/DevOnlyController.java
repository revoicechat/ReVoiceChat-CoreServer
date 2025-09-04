package fr.revoicechat.core.dev;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.arc.profile.IfBuildProfile;

@Path("/tests")
@IfBuildProfile("dev")
public class DevOnlyController {

  @GET
  @RolesAllowed("USER")
  @Path(("/secured-endpoint"))
  public DevOnlyData securedEndpoint() {
    return new DevOnlyData("secured-endpoint");
  }

  @GET
  @PermitAll
  @Path(("/error/throw"))
  public String error() {
    throw new UnsupportedOperationException("this method is here to test error log generation");
  }

  public record DevOnlyData(String data) {}
}
