package fr.revoicechat.core.security.jwt;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.core.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;

@Singleton
public class JwtService {
  private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String jwtIssuer;
  @ConfigProperty(name = "revoicechat.jwt.valid-day", defaultValue = "30")
  int jwtValidDay;

  public String get(final User user) {
    LOG.info("generate jwt token for user {}", user.getId());
    return Jwt.issuer(jwtIssuer)
              .subject(user.getLogin())
              .groups(user.getType().getRoles())
              .expiresAt(System.currentTimeMillis() + 1000L * 3600 * 24 * jwtValidDay)
              .sign();
  }
}
