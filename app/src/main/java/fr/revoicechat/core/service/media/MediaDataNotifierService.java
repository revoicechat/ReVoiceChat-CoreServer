package fr.revoicechat.core.service.media;

import fr.revoicechat.core.model.MediaData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class MediaDataNotifierService {

  private final Instance<MediaNotifier> mediaNotifiers;

  @Inject
  public MediaDataNotifierService(Instance<MediaNotifier> mediaNotifiers) {
    this.mediaNotifiers = mediaNotifiers;
  }

  public void notify(MediaData mediaData) {
    mediaNotifiers.stream()
                  .filter(notifier -> notifier.origin().equals(mediaData.getOrigin()))
                  .findFirst()
                  .ifPresent(notifier -> notifier.notify(mediaData));
  }
}
