package fr.revoicechat.security;

import static jakarta.servlet.http.HttpServletResponse.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {

  private static final String[] SWAGGER_WHITELIST = { "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**" };

  private final UsernameOnlyAuthenticationProvider authenticationProvider;
  private final ContentAwareAccessDeniedHandler contentAwareAccessDeniedHandler;

  public SecurityConfig(UsernameOnlyAuthenticationProvider authenticationProvider, final ContentAwareAccessDeniedHandler contentAwareAccessDeniedHandler) {
    this.authenticationProvider = authenticationProvider;
    this.contentAwareAccessDeniedHandler = contentAwareAccessDeniedHandler;
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
               .cors(Customizer.withDefaults())
               .authorizeHttpRequests(auth -> auth
                   .requestMatchers("/signup", "/login").permitAll()
                   .requestMatchers(SWAGGER_WHITELIST).permitAll()
                   .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                   .anyRequest().authenticated()
               )
               .exceptionHandling(e -> e.accessDeniedHandler(handleAccessDenied())
                                        .authenticationEntryPoint(handleUnauthorizedAccess()))
               .build();
  }

  private AccessDeniedHandler handleAccessDenied() {
    return (request, response, ignore) ->
        contentAwareAccessDeniedHandler.handle(request, response, SC_FORBIDDEN);
  }

  private AuthenticationEntryPoint handleUnauthorizedAccess() {
    return (request, response, ignore) ->
        contentAwareAccessDeniedHandler.handle(request, response, SC_UNAUTHORIZED);
  }
}
