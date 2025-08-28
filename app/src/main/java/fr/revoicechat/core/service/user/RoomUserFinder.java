package fr.revoicechat.core.service.user;

import java.util.UUID;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.UserRepository;

@ApplicationScoped
public class RoomUserFinder {

  private final UserRepository userRepository;

  public RoomUserFinder(final UserRepository userRepository) {this.userRepository = userRepository;}

  /**
   * @param room currently, the user has no server, so no rooms.
   *             so we cannot know the user by room.
   */
  public Stream<User> find(UUID room) {
    return userRepository.findAll().stream();
  }
}
