package fr.revoicechat.core.service.media;

import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaOrigin;

public interface MediaNotifier {

  void notify(MediaData mediaData);

  MediaOrigin origin();
}
