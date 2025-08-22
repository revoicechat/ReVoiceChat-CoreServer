package fr.revoicechat.stub;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.model.User;
import fr.revoicechat.security.UserHolder;

@Alternative
@ApplicationScoped
public class UserHolderMock implements UserHolder {
  @Inject EntityManager entityManager;

  private UUID user;

  @Override
  @Transactional
  public User get() {
    return entityManager.getReference(User.class, user);
  }

  public void set(final User user) {
    this.user = user.getId();
  }

  public void clean() {
    this.user = null;
  }
}
