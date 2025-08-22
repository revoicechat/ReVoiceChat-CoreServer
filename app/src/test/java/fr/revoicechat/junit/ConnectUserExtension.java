package fr.revoicechat.junit;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import fr.revoicechat.model.User;
import fr.revoicechat.stub.UserHolderMock;

@Order(1)
public class ConnectUserExtension implements BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

  @Inject UserHolderMock userHolder;
  @Inject EntityManager entityManager;
  @Inject TransactionTemplate transactionTemplate;

  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
    SpringExtension.getApplicationContext(context)
                   .getAutowireCapableBeanFactory()
                   .autowireBean(this);
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    transactionTemplate.executeWithoutResult(s -> {
      var user = new User();
      user.setId(UUID.randomUUID());
      user.setLogin("test");
      user.setDisplayName("Test");
      user.setCreatedDate(LocalDateTime.of(2020, 1, 1, 0, 0));
      entityManager.persist(user);
      userHolder.set(user);
    });
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    userHolder.clean();
  }
}
