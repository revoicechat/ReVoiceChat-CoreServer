package fr.revoicechat.core.service.emote;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.emote.EmoteNotification;
import fr.revoicechat.notification.representation.NotificationActionType;
import fr.revoicechat.core.service.media.MediaNotifier;
import fr.revoicechat.notification.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class EmoteMediaNotifier implements MediaNotifier {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final EmoteRetrieverService emoteRetrieverService;

  @Inject
  public EmoteMediaNotifier(EntityManager entityManager,
                            UserRepository userRepository,
                            EmoteRetrieverService emoteRetrieverService) {
    this.entityManager = entityManager;
    this.userRepository = userRepository;
    this.emoteRetrieverService = emoteRetrieverService;
  }

  @Override
  public void notify(MediaData mediaData, NotificationActionType actionType) {
    Emote emote = emoteRetrieverService.getEntity(mediaData.getId());
    var notification = Notification.of(new EmoteNotification(
        emoteRetrieverService.toRepresentation(emote),
        emote.getEntity(),
        actionType
    ));
    notifyUser(emote, notification);
    notifyServer(emote, notification);
  }

  public void notify(Emote emote, NotificationActionType actionType) {
    var notification = Notification.of(new EmoteNotification(
        emoteRetrieverService.toRepresentation(emote),
        emote.getEntity(),
        actionType
    ));
    notifyUser(emote, notification);
    notifyServer(emote, notification);
  }

  private void notifyUser(final Emote emote, final Notification notification) {
    User user = entityManager.find(User.class, emote.getEntity());
    if (user != null) {
      notification.sendTo(user);
    }
  }

  private void notifyServer(final Emote emote, final Notification notification) {
    Server server = entityManager.find(Server.class, emote.getEntity());
    if (server != null) {
      notification.sendTo(userRepository.findByServers(server.getId()));
    }
  }

  @Override
  public MediaOrigin origin() {
    return MediaOrigin.EMOTE;
  }
}
