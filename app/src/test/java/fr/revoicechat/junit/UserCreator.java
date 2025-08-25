package fr.revoicechat.junit;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import fr.revoicechat.model.User;
import fr.revoicechat.stub.UserHolderMock;

@ApplicationScoped
@Transactional
public class UserCreator {
  @PersistenceContext
  EntityManager entityManager;
  @Inject UserHolderMock userHolderMock;

  public User create() {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin("test");
    user.setDisplayName("Test");
    user.setCreatedDate(LocalDateTime.of(2020, 1, 1, 0, 0));
    entityManager.persist(user);
    userHolderMock.set(user);
    return user;
  }
}