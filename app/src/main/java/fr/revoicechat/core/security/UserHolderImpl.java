package fr.revoicechat.core.security;

import static fr.revoicechat.core.nls.CommonErrorCode.USER_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.jwt.JsonWebToken;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;

@ApplicationScoped
public class UserHolderImpl implements UserHolder {

  private final JWTParser jwtParser;
  private final UserRepository userRepository;
  private final SecurityIdentity securityIdentity;

  public UserHolderImpl(JWTParser jwtParser, UserRepository userRepository, SecurityIdentity securityIdentity) {
    this.jwtParser = jwtParser;
    this.userRepository = userRepository;
    this.securityIdentity = securityIdentity;
  }

  @Override
  public User get() {
    String username = securityIdentity.getPrincipal().getName();
    return getUser(username);
  }

  @Override
  public User get(final String jwtToken) {
    try {
      JsonWebToken jwt = jwtParser.parse(jwtToken);
      return getUser(jwt.getSubject());
    } catch (Exception e) {
      throw new WebApplicationException("Invalid token", 401);
    }
  }

  private User getUser(String login) {
    var user = userRepository.findByLogin(login);
    if (user == null) {
      throw new NotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
