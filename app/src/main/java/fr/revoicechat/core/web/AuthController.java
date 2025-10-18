package fr.revoicechat.core.web;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.core.representation.login.UserPassword;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.security.service.SecurityTokenService;
import fr.revoicechat.security.utils.PasswordUtils;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

  private final UserService userService;
  private final SecurityTokenService securityTokenService;

  public AuthController(final UserService userService, final SecurityTokenService securityTokenService) {
    this.userService = userService;
    this.securityTokenService = securityTokenService;
  }

  @Operation(summary = "Register a new user", description = "Creates a new user account with the provided signup details.")
  @APIResponse(responseCode = "200", description = "User successfully created",
      content = @Content(schema = @Schema(implementation = UserRepresentation.class))
  )
  @APIResponse(responseCode = "400",
      description = "Invalid input data",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, examples = "Invalid input data"))
  )
  @PUT
  @PermitAll
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/signup")
  public UserRepresentation signup(SignupRepresentation user) {
    return userService.create(user);
  }

  @Operation(summary = "Log in with displayName and password",
      description = "Authenticates the user with given credentials and creates a session. "
                    + "Future requests will be authenticated with this session.")
  @RequestBody(
      description = "The displayName and password of the user",
      content = @Content(schema = @Schema(implementation = UserPassword.class))
  )
  @APIResponse(responseCode = "200", description = "User successfully logged in")
  @APIResponse(responseCode = "401",
      description = "Invalid displayName or password",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, examples = "Invalid displayName or password"))
  )
  @POST
  @PermitAll
  @Path("/login")
  @Produces(MediaType.TEXT_PLAIN)
  public Response login(UserPassword request) {
    var user = userService.findByLogin(request.username());
    if (user != null && PasswordUtils.matches(request.password(), user.getPassword())) {
      return Response.ok(securityTokenService.generate(user)).build();
    } else {
      return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
    }
  }
}
