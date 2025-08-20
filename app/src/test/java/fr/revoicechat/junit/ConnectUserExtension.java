package fr.revoicechat.junit;

import java.util.UUID;
import jakarta.inject.Inject;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import fr.revoicechat.stub.UserHolderMock;

public class ConnectUserExtension implements BeforeEachCallback, TestInstancePostProcessor {

  @Inject UserHolderMock userHolder;
  @Inject UserRepository repository;

  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
    SpringExtension.getApplicationContext(context)
                   .getAutowireCapableBeanFactory()
                   .autowireBean(this);
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin("test");
    user.setDisplayName("Test");
    repository.save(user);
    userHolder.set(user);
  }
}
