package fr.revoicechat.web.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.Server;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("server")
@Tag(name = "Server", description = "Endpoints for managing server and their rooms")
public interface ServerController extends LoggedApi {

  @Operation(
      summary = "Get all servers",
      description = "Retrieve the list of all available servers.",
      tags = { "Server" },
      responses = { @ApiResponse(responseCode = "200", description = "List of servers successfully retrieved") }
  )
  @GetMapping
  List<Server> getServers();

  @Operation(
      summary = "Get a server by ID",
      description = "Retrieve details of a specific server by its unique identifier.",
      tags = { "Server" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Server successfully retrieved"),
          @ApiResponse(
              responseCode = "404",
              description = "Server not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Server not found")
              )
          )
      }
  )
  @GetMapping("/{id}")
  Server getServer(@PathVariable UUID id);

  @Operation(
      summary = "Create a new server",
      description = "Create a new server with the provided information.",
      tags = { "Server" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Server successfully created"),
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
  @PutMapping
  Server createServer(@RequestBody ServerCreationRepresentation representation);

  @Operation(
      summary = "Update an existing server",
      description = "Update the information of an existing server by its ID.",
      tags = { "Server" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Server successfully updated"),
          @ApiResponse(
              responseCode = "404",
              description = "Server not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Server not found")
              )
          )
      }
  )
  @PostMapping("/{id}")
  Server updateServer(@PathVariable UUID id,
                      @RequestBody ServerCreationRepresentation representation);

  @Operation(
      summary = "Get rooms for a server",
      description = "Retrieve the list of rooms belonging to a specific server.",
      tags = { "Server", "Room" },
      responses = {
          @ApiResponse(responseCode = "200", description = "List of rooms successfully retrieved"),
          @ApiResponse(
              responseCode = "404",
              description = "Server not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Server not found")
              )
          )
      }
  )
  @GetMapping("/{id}/room")
  List<Room> getRooms(@PathVariable final UUID id);

  @Operation(
      summary = "Create a new room in a server",
      description = "Add a new room to a specific server identified by its ID.",
      tags = { "Server", "Room" },
      responses = {
          @ApiResponse(responseCode = "200", description = "Room successfully created"),
          @ApiResponse(
              responseCode = "404",
              description = "Server not found",
              content = @Content(
                  mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "Server not found")
              )
          )
      }
  )
  @PutMapping("/{id}/room")
  Room createRoom(@PathVariable final UUID id, @RequestBody RoomRepresentation representation);
}
