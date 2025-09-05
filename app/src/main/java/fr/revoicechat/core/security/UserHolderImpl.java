package fr.revoicechat.core.security;

import static fr.revoicechat.core.nls.CommonErrorCode.USER_NOT_FOUND;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.jwt.JsonWebToken;

import fr.revoicechat.core.model.User;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;

@ApplicationScoped
public class UserHolderImpl implements UserHolder {

  private final JWTParser jwtParser;
  private final EntityManager entityManager;
  private final SecurityIdentity securityIdentity;

  public UserHolderImpl(JWTParser jwtParser, EntityManager entityManager, SecurityIdentity securityIdentity) {
    this.jwtParser = jwtParser;
    this.entityManager = entityManager;
    this.securityIdentity = securityIdentity;
  }

  @Override
  public User get() {
    var id = UUID.fromString(securityIdentity.getPrincipal().getName());
    return getUser(id);
  }

  @Override
  public User get(final String jwtToken) {
    try {
      return getUser(peekId(jwtToken));
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new WebApplicationException("Invalid token", 401);
    }
  }

  @Override
  public UUID peekId(final String jwtToken) {
    try {
      JsonWebToken jwt = jwtParser.parse(jwtToken);
      return UUID.fromString(jwt.getName());
    } catch (Exception e) {
      throw new WebApplicationException("Invalid token", 401);
    }
  }

  private User getUser(UUID id) {
    var user = entityManager.find(User.class, id);
    if (user == null) {
      throw new NotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
