package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import fr.revoicechat.junit.ClearDataBase;
import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.representation.login.UserPassword;
import fr.revoicechat.representation.user.SignupRepresentation;

/** @see AuthController */
@SpringBootTest
@ActiveProfiles({ "test", "test-h2" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ClearDataBase
class TestAuthController {

  @Inject AuthController authController;

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
    var wrongUserPassword = new UserPassword("testUser", "pswwwww");
    try (var response = authController.login(wrongUserPassword)) {
      assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
      assertThat(response.getEntity()).isEqualTo("Invalid credentials");
    }
    var goodUserPassword = new UserPassword("testUser", "psw");
    try (var result = authController.login(goodUserPassword)) {
      assertThat(result.getStatus()).isEqualTo(Status.OK.getStatusCode());
      assertThat(result.getEntity()).isEqualTo("User testUser logged in");
    }
  }
}