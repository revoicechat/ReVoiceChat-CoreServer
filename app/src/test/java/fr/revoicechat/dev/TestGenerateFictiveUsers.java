package fr.revoicechat.dev;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TestGenerateFictiveUsers {

  @Mock private EntityManager entityManager;
  @Mock private TransactionTemplate transactionTemplate;
  @Mock private UserRepository userRepository;
  @InjectMocks private GenerateFictiveUsers generateFictiveUsers;

  @Test
  void testGenerate() {
    doAnswer(invocationOnMock -> {
      Consumer<TransactionStatus> action = invocationOnMock.getArgument(0);
      action.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());
    doReturn(0L).when(userRepository).count();
    generateFictiveUsers.init();
    verify(entityManager, times(4)).persist(any(User.class));
  }

  @Test
  void testDoNotGenerate() {
    doReturn(1L).when(userRepository).count();
    generateFictiveUsers.init();
    verify(entityManager, never()).persist(any(User.class));
  }
}