package fr.revoicechat.security;

import static fr.revoicechat.nls.CommonErrorCode.USER_NOT_FOUND;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

public record UserHolderImpl(UserRepository userRepository) implements UserHolder {

  public User get() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    var user = userRepository.findByLogin(auth.getName());
    if (user == null) {
      throw new UsernameNotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
