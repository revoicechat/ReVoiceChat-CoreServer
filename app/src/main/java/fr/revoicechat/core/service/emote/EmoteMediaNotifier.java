package fr.revoicechat.core.service.emote;

import static fr.revoicechat.core.representation.notification.NotificationActionType.MODIFY;

import fr.revoicechat.core.model.Emote;
import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.emote.EmoteNotification;
import fr.revoicechat.core.service.media.MediaNotifier;
import fr.revoicechat.notification.Notification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class EmoteMediaNotifier implements MediaNotifier {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final InternalEmoteService internalEmoteService;

  @Inject
  public EmoteMediaNotifier(EntityManager entityManager,
                            UserRepository userRepository,
                            InternalEmoteService internalEmoteService) {
    this.entityManager = entityManager;
    this.userRepository = userRepository;
    this.internalEmoteService = internalEmoteService;
  }

  @Override
  public void notify(final MediaData mediaData) {
    Emote emote = internalEmoteService.getEntity(mediaData.getId());
    var notification = Notification.of(new EmoteNotification(
        internalEmoteService.toRepresentation(emote),
        emote.getEntity(),
        MODIFY
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
