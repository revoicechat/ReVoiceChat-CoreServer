package fr.revoicechat.core.service.emote;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.repository.EmoteRepository;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.web.error.ResourceNotFoundException;

@ApplicationScoped
public class EmoteRetrieverService {

  private final EntityManager entityManager;
  private final EmoteRepository emoteRepository;

  @Inject
  public EmoteRetrieverService(EntityManager entityManager, EmoteRepository emoteRepository) {
    this.entityManager = entityManager;
    this.emoteRepository = emoteRepository;
  }

  public Emote getEntity(final UUID id) {
    var emote = entityManager.find(Emote.class, id);
    if (emote == null) {
      throw new ResourceNotFoundException(Emote.class, id);
    }
    return emote;
  }

  public EmoteRepresentation get(final UUID id) {
    return toRepresentation(getEntity(id));
  }

  public EmoteRepresentation toRepresentation(Emote emote) {
    return new EmoteRepresentation(
        emote.getId(),
        emote.getContent(),
        new ArrayList<>(emote.getKeywords())
    );
  }

  @Transactional
  public List<EmoteRepresentation> getAll(final UUID id) {
    return emoteRepository.findByEntity(id).map(this::toRepresentation).toList();
  }
}
