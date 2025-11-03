package fr.revoicechat.core.service.message;

import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaOrigin;
import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.repository.MessageRepository;
import fr.revoicechat.core.representation.message.MessageNotification;
import fr.revoicechat.core.representation.notification.NotificationActionType;
import fr.revoicechat.core.service.MessageService;
import fr.revoicechat.core.service.media.MediaNotifier;
import fr.revoicechat.core.service.user.RoomUserFinder;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageMediaNotifier implements MediaNotifier {

  private final MessageRepository messageRepository;
  private final MessageService messageService;
  private final RoomUserFinder roomUserFinder;

  public MessageMediaNotifier(final MessageRepository messageRepository, final MessageService messageService, final RoomUserFinder roomUserFinder) {
    this.messageRepository = messageRepository;
    this.messageService = messageService;
    this.roomUserFinder = roomUserFinder;
  }

  @Override
  public void notify(final MediaData mediaData, NotificationActionType actionType) {
    var message = messageRepository.findByMedia(mediaData.getId());
    if (message == null) {
      throw new ResourceNotFoundException(Message.class, mediaData.getId());
    }
    Notification.of(new MessageNotification(messageService.toRepresentation(message), actionType))
                .sendTo(roomUserFinder.find(message.getRoom().getId()));
  }

  @Override
  public MediaOrigin origin() {
    return MediaOrigin.ATTACHMENT;
  }
}
