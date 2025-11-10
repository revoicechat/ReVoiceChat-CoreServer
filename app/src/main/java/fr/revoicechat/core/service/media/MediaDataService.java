package fr.revoicechat.core.service.media;

import static fr.revoicechat.core.model.MediaDataStatus.STORED;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.repository.MediaDataRepository;
import fr.revoicechat.core.representation.media.CreatedMediaDataRepresentation;
import fr.revoicechat.core.representation.media.MediaDataRepresentation;
import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MediaDataService {

  @ConfigProperty(name = "revoicechat.global.media-server-url")
  String mediaServerUrl;
  private final EntityManager entityManager;
  private final MediaDataRepository mediaDataRepository;
  private final FileTypeDetermination fileTypeDetermination;
  private final MediaDataNotifierService mediaDataNotifierService;

  public MediaDataService(final EntityManager entityManager,
                          final MediaDataRepository mediaDataRepository,
                          final FileTypeDetermination fileTypeDetermination,
                          final MediaDataNotifierService mediaDataNotifierService) {
    this.entityManager = entityManager;
    this.mediaDataRepository = mediaDataRepository;
    this.fileTypeDetermination = fileTypeDetermination;
    this.mediaDataNotifierService = mediaDataNotifierService;
  }

  public MediaData create(final CreatedMediaDataRepresentation creation, MediaOrigin origin) {
    MediaData mediaData = new MediaData();
    mediaData.setId(UUID.randomUUID());
    mediaData.setName(creation.name());
    mediaData.setType(fileTypeDetermination.get(creation.name()));
    mediaData.setOrigin(origin);
    mediaData.setStatus(MediaDataStatus.DOWNLOADING);
    entityManager.persist(mediaData);
    return mediaData;
  }

  public MediaDataRepresentation get(final UUID id) {
    return toRepresentation(getEntity(id));
  }

  @Transactional
  public MediaDataRepresentation update(final UUID id, final MediaDataStatus status) {
    var mediaData = getEntity(id);
    mediaData.setStatus(MediaDataStatus.valueOf(status.name()));
    entityManager.persist(mediaData);
    mediaDataNotifierService.notify(mediaData, status.equals(STORED) ? NotificationActionType.MODIFY
                                                                     : NotificationActionType.REMOVE);
    return toRepresentation(mediaData);
  }

  public MediaDataRepresentation toRepresentation(final MediaData media) {
    return new MediaDataRepresentation(
        media.getId(),
        media.getName(),
        mediaServerUrl + "/" + media.getName(),
        media.getOrigin(),
        media.getStatus(),
        media.getType()
    );
  }

  private MediaData getEntity(final UUID id) {
    var media = entityManager.find(MediaData.class, id);
    if (media == null) {
      throw new ResourceNotFoundException(MediaData.class, id);
    }
    return media;
  }

  public List<MediaDataRepresentation> findMediaByStatus(final MediaDataStatus status) {
    return mediaDataRepository.findByStatus(status).map(this::toRepresentation).toList();
  }
}
