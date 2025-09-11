package fr.revoicechat.core.web.api;

import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.representation.media.UpdatableMediaDataStatus;
import fr.revoicechat.core.representation.media.MediaDataRepresentation;

@Tag(name = "Media", description = "Endpoints for media data")
@Path("/media")
public interface MediaDataController extends LoggedApi {

  @Operation(summary = "Retrieve a media", description = "Retrieve a media")
  @APIResponse(responseCode = "200", description = "media successfully retrieved")
  @APIResponse(responseCode = "404", description = "Media not found", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, examples = "Media not found")))
  @APIResponse(responseCode = "401", description = "You have not the right to access this media")
  @GET
  @Path("/{id}")
  MediaDataRepresentation get(@PathParam("id") UUID id);

  @Operation(summary = "Update a media status", description = "Update a media status.")
  @APIResponse(responseCode = "200", description = "media successfully updated")
  @APIResponse(responseCode = "404", description = "Media not found", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, examples = "Media not found")))
  @APIResponse(responseCode = "401", description = "You have not the right to update this media")
  @Path("/{id}")
  @PATCH
  MediaDataRepresentation updateMediaByStatus(@PathParam("id") UUID id, UpdatableMediaDataStatus status);

  @Operation(summary = "Retrieve all medias with", description = "Retrieve all medias with a specific status.")
  @APIResponse(responseCode = "200", description = "media successfully retrieved")
  @GET
  List<MediaDataRepresentation> findMediaByStatus(@QueryParam("status") MediaDataStatus status);

  @Operation(summary = "Delete a media", description = "Update a media status to DELETING.")
  @APIResponse(responseCode = "200", description = "media successfully deleted")
  @Path("/{id}")
  @DELETE
  MediaDataRepresentation delete(@PathParam("id") UUID id);
}
