package fr.revoicechat.service.user;

import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@Component
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
