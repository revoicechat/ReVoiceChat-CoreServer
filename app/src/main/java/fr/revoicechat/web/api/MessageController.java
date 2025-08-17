package fr.revoicechat.web.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("message/{id}")
@Tag(name = "Message", description = "Endpoints for managing chat messages")
public interface MessageController extends LoggedApi {

  @Operation(
      summary = "Get a message",
      description = "Retrieve a message by its unique identifier.",
      tags = {"Message"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Message successfully retrieved"),
          @ApiResponse(
              responseCode = "404",
              description = "Message not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Message not found")
              )
          )
      }
  )
  @GetMapping
  MessageRepresentation read(@PathVariable("id") UUID id);

  @Operation(
      summary = "Update a message",
      description = "Partially update an existing message using its ID.",
      tags = {"Message"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Message successfully updated"),
          @ApiResponse(
              responseCode = "404",
              description = "Message not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Message not found")
              )
          )
      }
  )
  @PatchMapping
  MessageRepresentation update(@PathVariable("id") UUID id, @RequestBody CreatedMessageRepresentation representation);

  @Operation(
      summary = "Delete a message",
      description = "Delete a message by its unique identifier.",
      tags = {"Message"},
      responses = {
          @ApiResponse(responseCode = "204", description = "Message successfully deleted"),
          @ApiResponse(
              responseCode = "404",
              description = "Message not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Message not found")
              )
          )
      }
  )
  @DeleteMapping
  UUID delete(@PathVariable("id") UUID id);
}
