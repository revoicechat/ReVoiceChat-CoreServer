package fr.revoicechat.security;

import static fr.revoicechat.nls.CommonErrorCode.INVALID_CREDENTIALS;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import fr.revoicechat.repository.UserRepository;

@Component
public class UserPswAuthenticationProvider implements AuthenticationProvider {

  private final UserRepository userRepository;

  public UserPswAuthenticationProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    Object credential = authentication.getCredentials();
    var user = userRepository.findByLogin(username);
    if (user == null || !PasswordUtil.matches(credential.toString(), user.getPassword())) {
      throw new BadCredentialsException(INVALID_CREDENTIALS.translate());
    }
    return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}