package fr.revoicechat.core.web.api;

import java.util.UUID;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/settings")
@Tag(name = "Settings", description = "Endpoints for managing settings")
public interface UserSettingsController {

  @Tag(name = "User")
  @Operation(summary = "Get settings of the connected user", description = "Retrieve the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @GET
  @Path("/me")
  String me();

  @Tag(name = "User")
  @Operation(summary = "Get settings of the connected user", description = "Retrieve the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @GET
  @Path("/user/{id}")
  String ofUser(@PathParam("id") UUID id);

  @Tag(name = "User")
  @Operation(summary = "Update settings of the connected user", description = "Update the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @PATCH
  @Path("/me")
  String me(String settings);
}
