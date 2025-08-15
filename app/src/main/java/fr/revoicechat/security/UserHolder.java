package fr.revoicechat.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@Service
public class UserHolder {

  private final UserRepository userRepository;

  public UserHolder(final UserRepository userRepository) {this.userRepository = userRepository;}

  public User get() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    var user = userRepository.findByUsername(auth.getName());
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    return user;
  }
}
