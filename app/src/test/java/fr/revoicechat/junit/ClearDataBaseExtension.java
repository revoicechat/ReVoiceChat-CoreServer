package fr.revoicechat.junit;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

@Order(2)
public class ClearDataBaseExtension implements BeforeEachCallback, TestInstancePostProcessor {

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
      entityManager.flush();
      entityManager.clear();
      entityManager.getMetamodel().getEntities().forEach(entityType -> {
        String entityName = entityType.getName();
        entityManager.createQuery("DELETE FROM " + entityName).executeUpdate();
      });
    });
  }
}
