package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.web.api.AuthController;
import fr.revoicechat.web.api.AuthController.UserPassword;
import jakarta.inject.Inject;

@SpringBootTest
@ActiveProfiles({ "test", "test-h2" })
class TestAuthController {

  @Inject private AuthController authController;

  @Test
  void test() {
    var signup = new SignupRepresentation("testUser", "psw", "email@email.fr");
    var user = authController.signup(signup);
    assertThat(user).isNotNull();
    assertThat(user.id()).isNotNull();
    assertThat(user.displayName()).isEqualTo("testUser");
    assertThat(user.login()).isEqualTo("testUser");
    assertThat(user.createdDate()).isNotNull();
    assertThat(user.status()).isEqualTo(ActiveStatus.OFFLINE);
    var req = new MockHttpServletRequest();
    var wrongUserPassword = new UserPassword("testUser", "pswwwww");
    assertThatThrownBy(() -> authController.login(wrongUserPassword, req)).isInstanceOf(BadCredentialsException.class);
    var goodUserPassword = new UserPassword("testUser", "psw");
    var result = authController.login(goodUserPassword, req);
    assertThat(result).isEqualTo("User testUser logged in");
    var session = req.getSession(true);
    assertThat(session).isNotNull();
    assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();
  }
}