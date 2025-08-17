package fr.revoicechat.web.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.user.SignupRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(
    name = "Authentication",
    description = "Endpoints for user registration and login"
)
public interface AuthController {

  @PutMapping("/signup")
  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account with the provided signup details.",
      responses = {
          @ApiResponse(responseCode = "200", description = "User successfully created",
              content = @Content(schema = @Schema(implementation = User.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid input data",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Invalid input data")
              )
          )
      }
  )
  User signup(@RequestBody SignupRepresentation user);

  @Operation(
      summary = "Log in with username and password",
      description = "Authenticates the user with given credentials and creates a session. "
                    + "Future requests will be authenticated with this session.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "The username and password of the user",
          required = true,
          content = @Content(schema = @Schema(implementation = UserPassword.class))
      ),
      responses = {
          @ApiResponse(responseCode = "200", description = "User successfully logged in"),
          @ApiResponse(
              responseCode = "401",
              description = "Invalid username or password",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Invalid username or password")
              )
          )
      }
  )
  @PostMapping(value = "/login", consumes = { APPLICATION_JSON_VALUE })
  String login(@RequestBody UserPassword user, HttpServletRequest request);

  record UserPassword(String username, String password) {}
}
