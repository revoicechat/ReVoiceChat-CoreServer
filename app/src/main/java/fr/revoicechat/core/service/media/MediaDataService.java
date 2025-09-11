package fr.revoicechat.core.service.media;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.repository.MediaDataRepository;
import fr.revoicechat.core.representation.media.MediaDataRepresentation;
import fr.revoicechat.core.representation.media.UpdatableMediaDataStatus;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;

@ApplicationScoped
public class MediaDataService {

  @ConfigProperty(name = "revoicechat.global.media-server-url")
  String mediaServerUrl;
  private final EntityManager entityManager;
  private final MediaDataRepository mediaDataRepository;
  private final FileTypeDetermination fileTypeDetermination;

  public MediaDataService(EntityManager entityManager,
                          MediaDataRepository mediaDataRepository,
                          FileTypeDetermination fileTypeDetermination) {
    this.entityManager = entityManager;
    this.mediaDataRepository = mediaDataRepository;
    this.fileTypeDetermination = fileTypeDetermination;
  }

  public MediaData create(final CreatedMediaDataRepresentation creation) {
    MediaData mediaData = new MediaData();
    mediaData.setId(UUID.randomUUID());
    mediaData.setName(creation.name());
    mediaData.setType(fileTypeDetermination.get(creation.name()));
    mediaData.setOrigin(MediaOrigin.ATTACHMENT);
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
