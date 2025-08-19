package fr.revoicechat.security;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import fr.revoicechat.repository.UserRepository;

@Component
public class UsernameOnlyAuthenticationProvider implements AuthenticationProvider {

  private final UserRepository userRepository;

  public UsernameOnlyAuthenticationProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    if (userRepository.findByLogin(username) == null) {
      throw new BadCredentialsException("no user found with displayName %s".formatted(username));
    }
    return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}