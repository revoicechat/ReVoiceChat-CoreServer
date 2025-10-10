package fr.revoicechat.core.service.emote;

import static fr.revoicechat.risk.nls.RiskMembershipErrorCode.RISK_MEMBERSHIP_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.model.FileType;
import fr.revoicechat.core.nls.EmoteErrorCode;
import fr.revoicechat.core.repository.EmoteRepository;
import fr.revoicechat.core.representation.emote.CreationEmoteRepresentation;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.core.representation.media.CreatedMediaDataRepresentation;
import fr.revoicechat.core.service.media.MediaDataService;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import fr.revoicechat.risk.type.EmoteRiskType;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.BadRequestException;
import fr.revoicechat.web.error.ResourceNotFoundException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EmoteService {

  private final EmoteRepository emoteRepository;
  private final MediaDataService mediaDataService;
  private final EntityManager entityManager;
  private final UserHolder userHolder;
  private final RiskService riskService;

  @Inject
  public EmoteService(EmoteRepository emoteRepository,
                      MediaDataService mediaDataService,
                      EntityManager entityManager,
                      UserHolder userHolder,
                      RiskService riskService) {
    this.emoteRepository = emoteRepository;
    this.mediaDataService = mediaDataService;
    this.entityManager = entityManager;
    this.userHolder = userHolder;
    this.riskService = riskService;
  }

  public EmoteRepresentation get(final UUID id) {
    return toRepresentation(getEntity(id));
  }

  @Transactional
  public List<EmoteRepresentation> getAll(final UUID id) {
    return emoteRepository.findByEntity(id)
                          .map(this::toRepresentation)
                          .toList();
  }

  @Transactional
  public EmoteRepresentation add(final UUID id, final CreationEmoteRepresentation emote) {
    var media = mediaDataService.create(new CreatedMediaDataRepresentation(emote.fileName()));
    if (!media.getType().equals(FileType.PICTURE)) {
      throw new BadRequestException(EmoteErrorCode.ONLY_PICTURES_ERR);
    }
    Emote newEmote = new Emote();
    newEmote.setId(media.getId());
    newEmote.setContent(emote.content());
    newEmote.setKeywords(emote.keywords());
    newEmote.setEntity(id);
    newEmote.setMedia(media);
    entityManager.persist(newEmote);
    return toRepresentation(newEmote);
  }

  @Transactional
  public EmoteRepresentation update(final UUID id, final CreationEmoteRepresentation representation) {
    var user = userHolder.getId();
    var emote = getEntity(id);
    if (hasRisk(emote, user, EmoteRiskType.UPDATE_EMOTE)) {
      emote.setContent(representation.content());
      emote.setKeywords(representation.keywords());
      entityManager.persist(emote);
      return toRepresentation(emote);
    } else {
      throw new UnauthorizedException(RISK_MEMBERSHIP_ERROR.translate(EmoteRiskType.UPDATE_EMOTE));
    }
  }

  @Transactional
  public void delete(final UUID id) {
    var user = userHolder.getId();
    var emote = getEntity(id);
    if (hasRisk(emote, user, EmoteRiskType.REMOVE_EMOTE)) {
      entityManager.remove(emote);
    } else {
      throw new UnauthorizedException(RISK_MEMBERSHIP_ERROR.translate(EmoteRiskType.REMOVE_EMOTE));
    }
  }

  public Emote getEntity(final UUID id) {
    var emote = entityManager.find(Emote.class, id);
    if (emote == null) {
      throw new ResourceNotFoundException(Emote.class, id);
    }
    return emote;
  }

  private boolean hasRisk(final Emote emote, final UUID user, RiskType riskType) {
    return emote.getEntity().equals(user) ||
           riskService.hasRisk(user, new RiskEntity(emote.getEntity(), null), riskType);
  }

  private EmoteRepresentation toRepresentation(Emote emote) {
    return new EmoteRepresentation(
        emote.getId(),
        emote.getContent(),
        new ArrayList<>(emote.getKeywords())
    );
  }
}
