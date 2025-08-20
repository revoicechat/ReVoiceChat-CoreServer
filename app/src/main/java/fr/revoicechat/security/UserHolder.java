package fr.revoicechat.security;

import static fr.revoicechat.nls.CommonErrorCode.USER_NOT_FOUND;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@Service
public class UserHolder {

  private final UserRepository userRepository;

  public UserHolder(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User get() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    var user = userRepository.findByLogin(auth.getName());
    if (user == null) {
      throw new UsernameNotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
