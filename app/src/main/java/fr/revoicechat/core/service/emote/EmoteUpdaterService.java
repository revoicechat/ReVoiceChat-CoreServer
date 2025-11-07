package fr.revoicechat.core.service.emote;

import static fr.revoicechat.core.representation.notification.NotificationActionType.*;
import static fr.revoicechat.risk.nls.RiskMembershipErrorCode.RISK_MEMBERSHIP_ERROR;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.model.FileType;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.nls.EmoteErrorCode;
import fr.revoicechat.core.representation.emote.CreationEmoteRepresentation;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.core.representation.media.CreatedMediaDataRepresentation;
import fr.revoicechat.core.risk.EmoteRiskType;
import fr.revoicechat.core.service.emote.risk.EmoteRiskSupplier;
import fr.revoicechat.core.service.media.MediaDataNotifierService;
import fr.revoicechat.core.service.media.MediaDataService;
import fr.revoicechat.risk.type.RiskType;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.model.AuthenticatedUser;
import fr.revoicechat.web.error.BadRequestException;
import io.quarkus.security.UnauthorizedException;

@ApplicationScoped
public class EmoteUpdaterService {

  private final MediaDataService mediaDataService;
  private final EntityManager entityManager;
  private final UserHolder userHolder;
  private final EmoteRetrieverService emoteRetrieverService;
  private final MediaDataNotifierService mediaDataNotifierService;
  private final Instance<EmoteRiskSupplier> emoteRiskSuppliers;

  @Inject
  public EmoteUpdaterService(MediaDataService mediaDataService,
                             EntityManager entityManager,
                             UserHolder userHolder,
                             EmoteRetrieverService emoteRetrieverService,
                             MediaDataNotifierService mediaDataNotifierService,
                             Instance<EmoteRiskSupplier> emoteRiskSuppliers) {
    this.mediaDataService = mediaDataService;
    this.entityManager = entityManager;
    this.userHolder = userHolder;
    this.emoteRetrieverService = emoteRetrieverService;
    this.mediaDataNotifierService = mediaDataNotifierService;
    this.emoteRiskSuppliers = emoteRiskSuppliers;
  }

  @Transactional
  public EmoteRepresentation add(final UUID id, final CreationEmoteRepresentation emote) {
    var media = mediaDataService.create(new CreatedMediaDataRepresentation(emote.fileName()), MediaOrigin.EMOTE);
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
    return emoteRetrieverService.toRepresentation(newEmote);
  }

  @Transactional
  public EmoteRepresentation update(final UUID id, final CreationEmoteRepresentation representation) {
    var user = userHolder.get();
    var emote = emoteRetrieverService.getEntity(id);
    if (hasRisk(emote, user, EmoteRiskType.UPDATE_EMOTE)) {
      emote.setContent(representation.content());
      emote.setKeywords(representation.keywords());
      entityManager.persist(emote);
      mediaDataNotifierService.notify(emote.getMedia(), MODIFY);
      return emoteRetrieverService.toRepresentation(emote);
    } else {
      throw new UnauthorizedException(RISK_MEMBERSHIP_ERROR.translate(EmoteRiskType.UPDATE_EMOTE));
    }
  }

  @Transactional
  public void delete(final UUID id) {
    var user = userHolder.get();
    var emote = emoteRetrieverService.getEntity(id);
    if (hasRisk(emote, user, EmoteRiskType.REMOVE_EMOTE)) {
      entityManager.remove(emote);
      mediaDataNotifierService.notify(emote.getMedia(), REMOVE);
    } else {
      throw new UnauthorizedException(RISK_MEMBERSHIP_ERROR.translate(EmoteRiskType.REMOVE_EMOTE));
    }
  }

  private boolean hasRisk(final Emote emote, final AuthenticatedUser user, RiskType riskType) {
    return emoteRiskSuppliers.stream().anyMatch(emoteRiskSupplier -> emoteRiskSupplier.hasRisk(emote, user, riskType));
  }
}
