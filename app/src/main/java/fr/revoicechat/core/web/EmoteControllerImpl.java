package fr.revoicechat.core.web;

import static fr.revoicechat.security.utils.RevoiceChatRoles.ROLE_USER;

import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.representation.emote.CreationEmoteRepresentation;
import fr.revoicechat.core.representation.emote.EmoteRepresentation;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.service.emote.EmoteService;
import fr.revoicechat.core.web.api.EmoteController;
import fr.revoicechat.risk.RisksMembershipData;
import fr.revoicechat.risk.retriever.ServerIdRetriever;
import fr.revoicechat.security.UserHolder;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(ROLE_USER)
public class EmoteControllerImpl implements EmoteController {

  private final UserHolder userHolder;
  private final ServerService serverService;
  private final EmoteService emoteService;

  public EmoteControllerImpl(UserHolder userHolder, ServerService serverService, EmoteService emoteService) {
    this.userHolder = userHolder;
    this.serverService = serverService;
    this.emoteService = emoteService;
  }

  @Override
  public List<EmoteRepresentation> getMyEmotes() {
    var id = userHolder.getId();
    return emoteService.getAll(id);
  }

  @Override
  public EmoteRepresentation addToMyEmotes(final CreationEmoteRepresentation emote) {
    var id = userHolder.getId();
    return emoteService.add(id, emote);  }

  @Override
  public List<EmoteRepresentation> getServerEmotes(final UUID serverId) {
    var server = serverService.getEntity(serverId);
    return emoteService.getAll(server.getId());
  }

  @Override
  @RisksMembershipData(risks = "ADD_EMOTE", retriever = ServerIdRetriever.class)
  public EmoteRepresentation addToServerEmotes(final UUID serverId, final CreationEmoteRepresentation emote) {
    var server = serverService.getEntity(serverId);
    return emoteService.add(server.getId(), emote);
  }

  @Override
  public EmoteRepresentation getEmote(final UUID id) {
    return emoteService.get(id);
  }

  @Override
  public EmoteRepresentation patchEmote(final UUID id, final CreationEmoteRepresentation emote) {
    return emoteService.update(id, emote);
  }

  @Override
  public void deleteEmote(final UUID id) {
    emoteService.delete(id);
  }
}
