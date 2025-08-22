package fr.revoicechat.junit;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.revoicechat.model.User;
import fr.revoicechat.stub.UserHolderMock;

@Order(1)
public class ConnectUserExtension implements BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

  @Inject UserHolderMock userHolder;
  @Inject UserCreator user;

  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
    SpringExtension.getApplicationContext(context)
                   .getAutowireCapableBeanFactory()
                   .autowireBean(this);
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    userHolder.set(user.create());
  }

  @Override
  public void afterEach(final ExtensionContext context) {
    userHolder.clean();
  }

  @Named
  @Transactional
  static class UserCreator {
    @PersistenceContext
    EntityManager entityManager;

    User create() {
      var user = new User();
      user.setId(UUID.randomUUID());
      user.setLogin("test");
      user.setDisplayName("Test");
      user.setCreatedDate(LocalDateTime.of(2020, 1, 1, 0, 0));
      entityManager.persist(user);
      return user;
    }
  }
}
