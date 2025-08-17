package fr.revoicechat.web.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.revoicechat.representation.user.UserRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints for managing user")
public interface UserController extends LoggedApi {

  @Operation(
      summary = "Get details of the connected user",
      description = "Retrieve the details of the specific connected user.",
      tags = { "User" },
      responses = { @ApiResponse(responseCode = "200"), }
  )
  @GetMapping("/me")
  UserRepresentation me();

  @Operation(
      summary = "Get details of a user",
      description = "Retrieve the details of a specific user by its id.",
      tags = { "Room", "Message" },
      responses = {
          @ApiResponse(responseCode = "200"),
          @ApiResponse(
              responseCode = "404",
              description = "User not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "User not found")
              )
          )
      }
  )
  @GetMapping("/{id}")
  UserRepresentation get(@PathVariable UUID id);
}
