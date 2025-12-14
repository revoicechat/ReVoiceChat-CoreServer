package fr.revoicechat.core.dev;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.impl.UserRepositoryImpl;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.stub.EntityManagerMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TestGenerateFictiveUsers {

  private final ServerService serverService = new ServerService(null, null, null, null, null, null, null, null) {
    @Override
    public void joinDefaultServer(final User user) {
      // nothing here
    }
  };

  @Test
  void testGenerate() {
    // Given
    var entityManager = new EntityManagerMock() {
      int persist;
      @Override
      public void persist(final Object o) {persist++;}
    };
    var userRepository = new UserRepositoryImpl() {
      @Override
      public long count() {return 0L;}
    };
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager), serverService);
    process.canBeCalled = true;
    // When
    process.init();
    // Then
    assertThat(entityManager.persist).isEqualTo(2);
  }

  @Test
  void testDoNotGenerate() {
    // Given
    var entityManager = new EntityManagerMock() {
      int persist;
      @Override
      public void persist(final Object o) {persist++;}
    };
    var userRepository = new UserRepositoryImpl() {
      @Override
      public long count() {return 1L;}
    };
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager), serverService);
    process.canBeCalled = true;
    // When
    process.init();
    // Then
    assertThat(entityManager.persist).isZero();
  }

  @Test
  void testCannotBeCalled() {
    // Given
    var entityManager = new EntityManagerMock() {
      int persist;
      @Override
      public void persist(final Object o) {persist++;}
    };
    var userRepository = new UserRepositoryImpl() {
      @Override
      public long count() {return 1L;}
    };
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager), serverService);
    process.canBeCalled = false;
    // When
    process.init();
    // Then
    assertThat(entityManager.persist).isZero();
  }
}