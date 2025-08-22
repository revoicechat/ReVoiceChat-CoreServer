package fr.revoicechat.security;

import static fr.revoicechat.nls.CommonErrorCode.INVALID_CREDENTIALS;
import static fr.revoicechat.security.PasswordUtil.encodePassword;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestUserPswAuthenticationProvider {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserPswAuthenticationProvider provider;

  @Test
  void testNoUser() {
    doReturn(null).when(userRepository).findByLogin(any());
    Authentication auth = new UsernamePasswordAuthenticationToken("test", "test", Collections.emptyList());
    assertThatThrownBy(() -> provider.authenticate(auth))
              .isInstanceOf(BadCredentialsException.class)
              .hasMessage(INVALID_CREDENTIALS.translate());
  }

  @Test
  void testUserWithWrongPassword() {
    User user = new User();
    user.setPassword(encodePassword("password"));
    doReturn(user).when(userRepository).findByLogin(any());
    Authentication auth = new UsernamePasswordAuthenticationToken("test", "test", Collections.emptyList());
    assertThatThrownBy(() -> provider.authenticate(auth))
              .isInstanceOf(BadCredentialsException.class)
              .hasMessage(INVALID_CREDENTIALS.translate());
  }

  @Test
  void testUserWithGoodPassword() {
    User user = new User();
    user.setPassword(encodePassword("password"));
    doReturn(user).when(userRepository).findByLogin(any());
    Authentication auth = new UsernamePasswordAuthenticationToken("test", "password", Collections.emptyList());
    assertThatCode(() -> provider.authenticate(auth)).doesNotThrowAnyException();
  }

  @Test
  void testSupport() {
    assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
    assertThat(provider.supports(JaasAuthenticationToken.class)).isTrue();
    assertThat(provider.supports(TestingAuthenticationToken.class)).isFalse();
    assertThat(provider.supports(Object.class)).isFalse();
    assertThat(provider.supports(Authentication.class)).isFalse();
    assertThatThrownBy(() -> provider.supports(null)).isInstanceOf(NullPointerException.class);
  }
}