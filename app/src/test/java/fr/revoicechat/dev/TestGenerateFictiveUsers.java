package fr.revoicechat.dev;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TestGenerateFictiveUsers {

  @Mock private UserRepository userRepository;
  @InjectMocks private GenerateFictiveUsers generateFictiveUsers;

  @Test
  void testGenerate() {
    doReturn(0L).when(userRepository).count();
    generateFictiveUsers.init();
    verify(userRepository, times(4)).save(any(User.class));
  }

  @Test
  void testDoNotGenerate() {
    doReturn(1L).when(userRepository).count();
    generateFictiveUsers.init();
    verify(userRepository, never()).save(any(User.class));
  }
}