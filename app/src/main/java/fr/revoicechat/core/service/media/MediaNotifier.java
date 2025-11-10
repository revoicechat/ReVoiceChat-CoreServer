package fr.revoicechat.core.service.media;

import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.notification.representation.NotificationActionType;

public interface MediaNotifier {

  void notify(MediaData mediaData, NotificationActionType actionType);

  MediaOrigin origin();
}
