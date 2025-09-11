package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.List;
import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.representation.media.MediaDataRepresentation;
import fr.revoicechat.core.representation.media.UpdatableMediaDataStatus;
import fr.revoicechat.core.service.media.MediaDataService;
import fr.revoicechat.core.web.api.MediaDataController;

@RolesAllowed(ROLE_USER)
public class MediaDataControllerImpl implements MediaDataController {

  private final MediaDataService mediaDataService;

  public MediaDataControllerImpl(final MediaDataService mediaDataService) {
    this.mediaDataService = mediaDataService;
  }

  @Override
  public MediaDataRepresentation get(final UUID id) {
    return mediaDataService.get(id);
  }

  @Override
  public MediaDataRepresentation updateMediaByStatus(final UUID id, final UpdatableMediaDataStatus status) {
    return mediaDataService.update(id, MediaDataStatus.valueOf(status.name()));
  }

  @Override
  public List<MediaDataRepresentation> findMediaByStatus(final MediaDataStatus status) {
    return mediaDataService.findMediaByStatus(status);
  }

  @Override
  public MediaDataRepresentation delete(final UUID id) {
    return mediaDataService.update(id, MediaDataStatus.DELETING);
  }
}
