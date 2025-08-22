package fr.revoicechat.junit;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Order(2)
public class ClearDataBaseExtension implements BeforeEachCallback, TestInstancePostProcessor {

  @Inject DBCleaner cleaner;

  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
    SpringExtension.getApplicationContext(context)
                   .getAutowireCapableBeanFactory()
                   .autowireBean(this);
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    cleaner.clean();
  }

  @Named
  @Transactional
  static class DBCleaner {
    @PersistenceContext
    EntityManager entityManager;

    void clean() {
      entityManager.flush();
      entityManager.clear();
      entityManager.getMetamodel().getEntities().forEach(entityType -> {
        String entityName = entityType.getName();
        entityManager.createQuery("DELETE FROM " + entityName).executeUpdate();
      });
    }
  }
}
