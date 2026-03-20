package fr.revoicechat.moderation.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.moderation.model.Sanction;
import fr.revoicechat.moderation.representation.NewSanction;
import fr.revoicechat.moderation.representation.SanctionNotification;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.notification.model.NotificationRegistrable.OnlineNotificationRegistrable;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.web.error.ResourceNotFoundException;

@ApplicationScoped
public class SanctionCreator {

  private final UserHolder userHolder;
  private final EntityManager entityManager;
  private final SanctionEntityService sanctionEntityService;

  public SanctionCreator(UserHolder userHolder,
                         EntityManager entityManager,
                         SanctionEntityService sanctionEntityService) {
    this.userHolder = userHolder;
    this.entityManager = entityManager;
    this.sanctionEntityService = sanctionEntityService;
  }

  @Transactional
  public Sanction create(final UUID serverId, final NewSanction newSanction) {
    Sanction sanction = new Sanction();
    sanction.setId(UUID.randomUUID());
    sanction.setTargetedUser(newSanction.targetedUser());
    sanction.setServer(serverId);
    sanction.setType(newSanction.type());
    sanction.setStartAt(LocalDateTime.now());
    sanction.setExpiresAt(newSanction.expiresAt());
    sanction.setIssuedBy(userHolder.getId());
    sanction.setReason(newSanction.reason());
    entityManager.persist(sanction);
    Notification.of(new SanctionNotification(sanction)).sendTo(new OnlineNotificationRegistrable(sanction.getTargetedUser()));
    return sanction;
  }

  @Transactional
  public void revoke(final UUID serverId, final UUID id) {
    var sanction = sanctionEntityService.get(id);
    if (!Objects.equals(sanction.getServer(), serverId)) {
      throw new ResourceNotFoundException(Sanction.class, id);
    }
    sanction.setRevokedBy(userHolder.getId());
    sanction.setRevokedAt(LocalDateTime.now());
    entityManager.persist(sanction);
    Notification.of(new SanctionNotification(sanction)).sendTo(new OnlineNotificationRegistrable(sanction.getTargetedUser()));
  }
}
