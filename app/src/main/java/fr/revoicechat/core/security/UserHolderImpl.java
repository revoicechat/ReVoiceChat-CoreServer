package fr.revoicechat.core.security;

import static fr.revoicechat.core.nls.CommonErrorCode.USER_NOT_FOUND;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import fr.revoicechat.core.model.User;
import fr.revoicechat.notification.NotificationRegistrableHolder;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.service.SecurityTokenService;
import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class UserHolderImpl implements UserHolder, NotificationRegistrableHolder {

  private final SecurityTokenService tokenService;
  private final SecurityIdentity securityIdentity;
  private final EntityManager entityManager;

  public UserHolderImpl(SecurityTokenService tokenService,
                        SecurityIdentity securityIdentity,
                        EntityManager entityManager) {
    this.tokenService = tokenService;
    this.securityIdentity = securityIdentity;
    this.entityManager = entityManager;
  }

  @Override
  @SuppressWarnings("unchecked")
  public User get() {
    var id = UUID.fromString(securityIdentity.getPrincipal().getName());
    return getUser(id);
  }

  @Override
  public UUID getId() {
    return get().getId();
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
    return tokenService.retrieveUserAsId(jwtToken);
  }

  private User getUser(UUID id) {
    var user = entityManager.find(User.class, id);
    if (user == null) {
      throw new NotFoundException(USER_NOT_FOUND.translate());
    }
    return user;
  }
}
