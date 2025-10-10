package fr.revoicechat.core.web.api;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.core.representation.emote.CreationEmoteRepresentation;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.openapi.api.LoggedApi;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Tag(name = "Emote", description = "Endpoints for emote")
@Path("emote")
public interface EmoteController extends LoggedApi {

  @Operation(summary = "Get emote of connected user")
  @APIResponse(responseCode = "200", description = "emotes successfully retrieved")
  @GET
  @Path("/me")
  List<EmoteRepresentation> getMyEmotes();

  @Operation(summary = "Add an emote to the connected user")
  @APIResponse(responseCode = "200", description = "emotes successfully added")
  @PUT
  @Path("/me")
  EmoteRepresentation addToMyEmotes(CreationEmoteRepresentation emote);

  @Operation(summary = "Get emote of a server")
  @APIResponse(responseCode = "200", description = "emotes successfully retrieved")
  @GET
  @Path("/server/{id}")
  List<EmoteRepresentation> getServerEmotes(@PathParam("id") UUID serverId);

  @Operation(summary = "Add an in a server")
  @APIResponse(responseCode = "200", description = "emotes successfully added")
  @PUT
  @Path("/server/{id}")
  EmoteRepresentation addToServerEmotes(@PathParam("id") UUID serverId, CreationEmoteRepresentation emote);

  @Operation(summary = "Get emote")
  @APIResponse(responseCode = "200", description = "emotes successfully retrieved")
  @GET
  @Path("/{id}")
  EmoteRepresentation getEmote(@PathParam("id") UUID id);

  @Operation(summary = "Patch an emote")
  @APIResponse(responseCode = "200", description = "emotes successfully updated")
  @PATCH
  @Path("/{id}")
  EmoteRepresentation patchEmote(@PathParam("id") UUID id, CreationEmoteRepresentation emote);

  @Operation(summary = "Delete an emote")
  @APIResponse(responseCode = "200", description = "emotes successfully deleted")
  @DELETE
  @Path("/{id}")
  void deleteEmote(@PathParam("id") UUID id);
}
