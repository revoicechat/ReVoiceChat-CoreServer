package fr.revoicechat.core.mapper;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.notification.Notification;
import fr.revoicechat.web.mapper.RepresentationMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper implements RepresentationMapper<User, UserRepresentation> {
  @Override
  public UserRepresentation map(final User user) {
    return new UserRepresentation(
        user.getId(),
        user.getDisplayName(),
        user.getLogin(),
        user.getCreatedDate(),
        Notification.ping(user),
        user.getType()
    );
  }
}
