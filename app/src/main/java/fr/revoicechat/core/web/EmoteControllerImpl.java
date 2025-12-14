package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.*;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.service.server.ServerEntityService;
import jakarta.annotation.security.RolesAllowed;

import fr.revoicechat.core.representation.emote.CreationEmoteRepresentation;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.service.emote.EmoteRetrieverService;
import fr.revoicechat.core.service.emote.EmoteUpdaterService;
import fr.revoicechat.core.web.api.EmoteController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import fr.revoicechat.security.UserHolder;

public class EmoteControllerImpl implements EmoteController {

  private final UserHolder userHolder;
  private final ServerEntityService serverService;
  private final EmoteUpdaterService emoteUpdaterService;
  private final EmoteRetrieverService emoteRetrieverService;

  public EmoteControllerImpl(UserHolder userHolder, ServerEntityService serverService, EmoteUpdaterService emoteUpdaterService, EmoteRetrieverService emoteRetrieverService) {
    this.userHolder = userHolder;
    this.serverService = serverService;
    this.emoteUpdaterService = emoteUpdaterService;
    this.emoteRetrieverService = emoteRetrieverService;
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<EmoteRepresentation> getMyEmotes() {
    var id = userHolder.getId();
    return emoteRetrieverService.getAll(id);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public EmoteRepresentation addToMyEmotes(final CreationEmoteRepresentation emote) {
    var id = userHolder.getId();
    return emoteUpdaterService.add(id, emote);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<EmoteRepresentation> getGlobalEmotes() {
    return emoteRetrieverService.getAll(null);
  }

  @Override
  @RolesAllowed(ROLE_ADMIN)
  public EmoteRepresentation addToGlobalEmotes(final CreationEmoteRepresentation emote) {
    return emoteUpdaterService.add(null, emote);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public List<EmoteRepresentation> getServerEmotes(final UUID serverId) {
    var server = serverService.getEntity(serverId);
    return emoteRetrieverService.getAll(server.getId());
  }

  @Override
  @RolesAllowed(ROLE_USER)
  @RisksMembershipData(risks = "ADD_EMOTE", retriever = ServerIdRetriever.class)
  public EmoteRepresentation addToServerEmotes(final UUID serverId, final CreationEmoteRepresentation emote) {
    var server = serverService.getEntity(serverId);
    return emoteUpdaterService.add(server.getId(), emote);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public EmoteRepresentation getEmote(final UUID id) {
    return emoteRetrieverService.get(id);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public EmoteRepresentation patchEmote(final UUID id, final CreationEmoteRepresentation emote) {
    return emoteUpdaterService.update(id, emote);
  }

  @Override
  @RolesAllowed(ROLE_USER)
  public void deleteEmote(final UUID id) {
    emoteUpdaterService.delete(id);
  }
}
