package fr.revoicechat.security.jwt;

import java.util.Set;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.revoicechat.model.User;
import io.smallrye.jwt.build.Jwt;

@Singleton
public class JwtService {

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String jwtIssuer;
  @ConfigProperty(name = "revoicechat.jwt.valid-day", defaultValue = "30")
  int jwtValidDay;

  public String get(final User user) {
    return Jwt.issuer(jwtIssuer)
              .subject(user.getLogin())
              .groups(Set.of("USER"))
              .expiresAt(System.currentTimeMillis() + 1000L * 3600 * 24 * jwtValidDay)
              .sign();
  }
}
