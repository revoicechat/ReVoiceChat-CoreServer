package fr.revoicechat.core.web.api;

import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.openapi.api.LoggedApi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/settings")
@Tag(name = "Settings", description = "Endpoints for managing settings")
public interface SettingsController extends LoggedApi  {

  @Operation(summary = "Get general settings", description = "Retrieve the settings of the app.")
  @APIResponse(responseCode = "200")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  Map<String, Object> genealSetings();

  @Tag(name = "User")
  @Operation(summary = "Get settings of the connected user", description = "Retrieve the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  @Path("/me")
  String me();

  @Tag(name = "User")
  @Operation(summary = "Get settings of the connected user", description = "Retrieve the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  @Path("/user/{id}")
  String ofUser(@PathParam("id") UUID id);

  @Tag(name = "User")
  @Operation(summary = "Update settings of the connected user", description = "Update the settings of the connected user.")
  @APIResponse(responseCode = "200")
  @Produces(MediaType.APPLICATION_JSON)
  @PATCH
  @Path("/me")
  String me(String settings);
}
