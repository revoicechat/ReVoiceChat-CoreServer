package fr.revoicechat.core.service.emote;

import java.util.ArrayList;
import java.util.UUID;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class InternalEmoteService {

  private final EntityManager entityManager;

  @Inject
  public InternalEmoteService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Emote getEntity(final UUID id) {
    var emote = entityManager.find(Emote.class, id);
    if (emote == null) {
      throw new ResourceNotFoundException(Emote.class, id);
    }
    return emote;
  }

  public EmoteRepresentation toRepresentation(Emote emote) {
    return new EmoteRepresentation(
        emote.getId(),
        emote.getContent(),
        new ArrayList<>(emote.getKeywords())
    );
  }
}
