package fr.revoicechat.web.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.revoicechat.model.Room;
import fr.revoicechat.repository.page.PageResult;
import fr.revoicechat.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.room.RoomRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("room/{id}")
@Tag(name = "Room", description = "Endpoints for managing chat rooms and their messages")
public interface RoomController extends LoggedApi {

  @Operation(
      summary = "Get room details",
      description = "Retrieve the details of a room by its unique identifier.",
      tags = { "Room" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Room successfully retrieved"),
          @ApiResponse(
              responseCode = "404",
              description = "Room not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Room not found")
              )
          )
      }
  )
  @GetMapping
  Room read(@PathVariable("id") UUID roomId);

  @Operation(
      summary = "Update a room",
      description = "Update the properties of an existing room using its ID.",
      tags = { "Room" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Room successfully updated"),
          @ApiResponse(
              responseCode = "404",
              description = "Room not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Room not found")
              )
          )
      }
  )
  @PatchMapping
  Room update(@PathVariable("id") UUID roomId,
              @RequestBody RoomRepresentation representation);

  @Operation(
      summary = "Delete a room",
      description = "Delete an existing room by its ID. Returns the deleted room's ID.",
      tags = { "Room" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Room successfully deleted"),

          @ApiResponse(
              responseCode = "404",
              description = "Room not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Room not found")
              )
          )
      }
  )
  @DeleteMapping
  UUID delete(@PathVariable("id") UUID roomId);

  @Operation(
      summary = "Get messages in a room",
      description = "Retrieve all messages from a specific room.",
      tags = { "Room", "Message" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Messages successfully retrieved"),
          @ApiResponse(
              responseCode = "404",
              description = "Room not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Room not found")
              )
          )
      }
  )
  @GetMapping("/message")
  PageResult<MessageRepresentation> messages(@PathVariable("id") UUID roomId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size);

  @Operation(
      summary = "Send a message in a room",
      description = "Create and send a new message inside a specific room.",
      tags = { "Room", "Message" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Message successfully created"),
          @ApiResponse(
              responseCode = "404",
              description = "Room not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Room not found")
              )
          )
      }
  )
  @PutMapping("/message")
  MessageRepresentation sendMessage(@PathVariable("id") UUID roomId,
                                    @RequestBody CreatedMessageRepresentation representation);
}
