package fr.revoicechat.core.service.profil;

import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.profilpicture.ProfilPictureUpdate;
import fr.revoicechat.core.risk.ServerRiskType;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.risk.service.RiskService;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public final class ServerPictureUpdater implements PictureUpdater<Server> {

  private final RiskService riskService;
  private final EntityManager entityManager;
  private final UserRepository userRepository;

  public ServerPictureUpdater(final RiskService riskService, EntityManager entityManager, UserRepository userRepository) {
    this.riskService = riskService;
    this.entityManager = entityManager;
    this.userRepository = userRepository;
  }

  @Override
  public Server get(final UUID id) {
    return entityManager.find(Server.class, id);
  }

  @Override
  public void emmit(final Server server) {
    if (riskService.hasRisk(new RiskEntity(server.getId(), null), ServerRiskType.SERVER_UPDATE)) {
      Notification.of(new ProfilPictureUpdate(server.getId())).sendTo(userRepository.findByServers(server.getId()));
    }
  }
}