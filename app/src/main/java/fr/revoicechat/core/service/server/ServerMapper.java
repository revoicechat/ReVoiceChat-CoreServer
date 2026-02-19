package fr.revoicechat.core.service.server;

import java.util.Optional;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.risk.ServerRiskType;
import fr.revoicechat.core.service.room.RoomReadStatusService;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerMapper {

  private final RiskService riskService;
  private final RoomReadStatusService roomReadStatusService;

  public ServerMapper(final RiskService riskService, final RoomReadStatusService roomReadStatusService) {
    this.riskService = riskService;
    this.roomReadStatusService = roomReadStatusService;
  }

  public ServerRepresentation map(final Server server) {
    return new ServerRepresentation(
        server.getId(),
        server.getName(),
        Optional.ofNullable(server.getOwner()).map(User::getId).orElse(null),
        roomReadStatusService.getUnreadMessagesStatus(server),
        riskService.hasRisk(new RiskEntity(server.getId(), null), ServerRiskType.SERVER_UPDATE)
    );
  }

  public ServerRepresentation mapLight(final Server server) {
    return new ServerRepresentation(
        server.getId(),
        server.getName(),
        Optional.ofNullable(server.getOwner()).map(User::getId).orElse(null),
        null,
        false
    );
  }
}
