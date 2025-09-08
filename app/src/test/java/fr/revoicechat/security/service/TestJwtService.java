package fr.revoicechat.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import fr.revoicechat.security.model.AuthenticatedUser;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

@QuarkusTest
class TestJwtService {

  @Inject SecurityTokenService jwtService;
  @Inject JWTParser jwtParser;

  @Test
  void test() throws ParseException {
    var id = UUID.randomUUID();
    var user = new AuthenticatedUserMock(id, "user name", "login", Set.of("USER", "OTHER"));
    var token = jwtService.generate(user);
    var result = jwtParser.parse(token);
    assertThat(result).isNotNull();
    assertThat(result.getGroups()).containsExactlyInAnyOrder("USER", "OTHER");
    assertThat(result.getName()).isEqualTo(id.toString());
    assertThat(result.getSubject()).isEqualTo("login");
    assertThat(jwtService.retrieveUserAsId(token)).isEqualTo(id);
  }

  private record AuthenticatedUserMock(
      UUID getId,
      String getDisplayName,
      String getLogin,
      Set<String> getRoles
  ) implements AuthenticatedUser {}
}