package fr.revoicechat.dev;

import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TestGenerateFictiveUsers {

  @Mock private EntityManager entityManager;
  @Mock private UserRepository userRepository;

  @Test
  void testGenerate() {
    doReturn(0L).when(userRepository).count();
    new GenerateFictiveUsers(userRepository, new UserCreator(entityManager)).init();
    verify(entityManager, times(4)).persist(any(User.class));
  }

  @Test
  void testDoNotGenerate() {
    doReturn(1L).when(userRepository).count();
    new GenerateFictiveUsers(userRepository, new UserCreator(entityManager)).init();
    verify(entityManager, never()).persist(any(User.class));
  }
}