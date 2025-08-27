package fr.revoicechat.dev;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.revoicechat.repository.impl.UserRepositoryImpl;
import fr.revoicechat.stub.EntityManagerMock;

class TestGenerateFictiveUsers {

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
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager));
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
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager));
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
    var process = new GenerateFictiveUsers(userRepository, new UserCreator(entityManager));
    process.canBeCalled = false;
    // When
    process.init();
    // Then
    assertThat(entityManager.persist).isZero();
  }
}