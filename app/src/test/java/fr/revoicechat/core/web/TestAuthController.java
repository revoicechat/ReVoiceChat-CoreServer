package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.notification.model.ActiveStatus;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.representation.login.UserPassword;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

/** @see AuthController */
@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
@CleanDatabase
class TestAuthController {

  @Inject JWTParser jwtParser;

  @Test
  void testSignup() {
    var response = signup();
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
    var user = response.as(UserRepresentation.class);
    assertThat(user).isNotNull();
    assertThat(user.id()).isNotNull();
    assertThat(user.displayName()).isEqualTo("testUser");
    assertThat(user.login()).isEqualTo("testUser");
    assertThat(user.createdDate()).isNotNull();
    assertThat(user.status()).isEqualTo(ActiveStatus.OFFLINE);
  }

  @Test
  void testLogin() throws ParseException {
    var sign = signup();
    var user = sign.as(UserRepresentation.class);
    var response = login("testUser", "psw");
    assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
    var token = response.asString();
    assertThat(token).isNotNull();
    var jwt = jwtParser.parse(token);
    assertThat(jwt.getName()).isEqualTo(user.id().toString());
    assertThat(jwt.getSubject()).isEqualTo("testUser");
  }

  @Test
  void testLoginWrongPassword() {
    signup();
    var response = login("testUser", "pswwwww");
    assertThat(response.getStatusCode()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.asString()).isEqualTo("Invalid credentials");
  }

  @Test
  void testLoginWrongUsername() {
    signup();
    var response = login("not an existing username", "psw");
    assertThat(response.getStatusCode()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    assertThat(response.asString()).isEqualTo("Invalid credentials");
  }

  private static Response signup() {
    var signup = new SignupRepresentation("testUser", "psw", "email@email.fr", UUID.randomUUID());
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(signup)
                      .when().put("/auth/signup");
  }

  private static Response login(String userName, String password) {
    var wrongUserPassword = new UserPassword(userName, password);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(wrongUserPassword)
                      .when().post("/auth/login");
  }
}