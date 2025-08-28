package fr.revoicechat.core.service.media;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation.CreatedMediaDataRepresentation;

@ApplicationScoped
public class MediaDataService {

  private final EntityManager entityManager;
  private final FileTypeDetermination fileTypeDetermination;

  public MediaDataService(EntityManager entityManager, final FileTypeDetermination fileTypeDetermination) {
    this.entityManager = entityManager;
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
}
