package fr.revoicechat.security;

import static fr.revoicechat.nls.CommonErrorCode.USER_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class UserHolderImpl implements UserHolder {

  private final UserRepository userRepository;
  private final SecurityIdentity securityIdentity;

  public UserHolderImpl(final UserRepository userRepository, final SecurityIdentity securityIdentity) {
    this.userRepository = userRepository;
    this.securityIdentity = securityIdentity;
  }

  @Override
  public User get() {
    String username = securityIdentity.getPrincipal().getName();
    var user = userRepository.findByLogin(username);
    if (user == null) {
      throw new NotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
