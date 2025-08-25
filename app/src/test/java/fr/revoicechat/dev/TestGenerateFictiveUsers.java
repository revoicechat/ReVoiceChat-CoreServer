package fr.revoicechat.dev;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.revoicechat.repository.impl.UserRepositoryImpl;
import fr.revoicechat.stub.EntityManagerMock;

class TestGenerateFictiveUsers {

  @Test
  void testGenerate() {
    var entityManager = new EntityManagerMock() {
      int persist;
      @Override
      public void persist(final Object o) {persist++;}
    };
    var userRepository = new UserRepositoryImpl() {
      @Override
      public long count() {return 0L;}
    };
    new GenerateFictiveUsers(userRepository, new UserCreator(entityManager)).init();
    assertThat(entityManager.persist).isEqualTo(4);
  }

  @Test
  void testDoNotGenerate() {
    var entityManager = new EntityManagerMock() {
      int persist;
      @Override
      public void persist(final Object o) {persist++;}
    };
    var userRepository = new UserRepositoryImpl() {
      @Override
      public long count() {return 1L;}
    };
    new GenerateFictiveUsers(userRepository, new UserCreator(entityManager)).init();
    assertThat(entityManager.persist).isZero();
  }
}