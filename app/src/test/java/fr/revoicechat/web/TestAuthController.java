package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.junit.DBCleaner;
import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.quarkus.profile.H2Profile;
import fr.revoicechat.representation.login.UserPassword;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.web.TestAuthController.NoInvitationProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

/** @see AuthController */
@QuarkusTest
@TestProfile(NoInvitationProfile.class)
class TestAuthController {

  @Inject DBCleaner dbCleaner;
  @Inject AuthController authController;
  @Inject JWTParser jwtParser;

  @BeforeEach
  @Transactional
  void setUp() {
    dbCleaner.clean();
  }

  @Test
  void test() throws ParseException {
    var signup = new SignupRepresentation("testUser", "psw", "email@email.fr", UUID.randomUUID());
    var user = authController.signup(signup);
    assertThat(user).isNotNull();
    assertThat(user.id()).isNotNull();
    assertThat(user.displayName()).isEqualTo("testUser");
    assertThat(user.login()).isEqualTo("testUser");
    assertThat(user.createdDate()).isNotNull();
    assertThat(user.status()).isEqualTo(ActiveStatus.OFFLINE);
    var wrongUserPassword = new UserPassword("testUser", "pswwwww");
    try (var response = authController.login(wrongUserPassword)) {
      assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
      assertThat(response.getEntity()).isEqualTo("Invalid credentials");
    }
    var goodUserPassword = new UserPassword("testUser", "psw");
    try (var result = authController.login(goodUserPassword)) {
      assertThat(result.getStatus()).isEqualTo(Status.OK.getStatusCode());
      String entity = (String) result.getEntity();
      assertThat(entity).isNotNull();
      var jwt = jwtParser.parse(entity);
      assertThat(jwt.getSubject()).isEqualTo("testUser");
    }
  }

  public static class NoInvitationProfile extends H2Profile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      return config;
    }
  }
}