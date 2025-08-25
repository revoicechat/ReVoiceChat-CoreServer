package fr.revoicechat.service;

import java.util.Set;
import jakarta.inject.Singleton;

import fr.revoicechat.model.User;
import io.smallrye.jwt.build.Jwt;

@Singleton
public class JwtService {


  public String get(final User user) {
    return Jwt.issuer("revoice-jwt")
              .subject("revoice-jwt")
              .groups(Set.of("USER"))
              .expiresAt(System.currentTimeMillis() + 3600)
              .sign();
  }
}
