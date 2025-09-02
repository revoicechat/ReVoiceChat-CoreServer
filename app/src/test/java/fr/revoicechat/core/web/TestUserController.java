package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
@CleanDatabase
class TestUserController {

  @Test
  @TestSecurity(user = "test-user", roles = { "USER" })
  void testGetUser() {
    var signedUser = RestTestUtils.signup("nyphew", "psw");
    var retrievedUser = RestAssured.given()
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .when().pathParam("id", signedUser.id()).get("/user/{id}")
                                   .then().statusCode(200)
                                   .extract().body().as(UserRepresentation.class);
    assertThat(retrievedUser).usingRecursiveComparison()
                             .withComparatorForType(Comparator.comparing(a -> a.truncatedTo(ChronoUnit.MILLIS)), OffsetDateTime.class)
                             .isEqualTo(signedUser);
  }

  @Test
  void testMe() {
    RestTestUtils.signup("rex_woof", "psw1");
    var signedUser = RestTestUtils.signup("nyphew", "psw2");
    var token = RestTestUtils.login("nyphew", "psw2");
    var retrievedUser = RestAssured.given()
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .header("Authorization", "Bearer " + token)
                                   .when().get("/user/me")
                                   .then().statusCode(200)
                                   .extract().body().as(UserRepresentation.class);
    assertThat(retrievedUser).usingRecursiveComparison()
                             .withComparatorForType(Comparator.comparing(a -> a.truncatedTo(ChronoUnit.MILLIS)), OffsetDateTime.class)
                             .isEqualTo(signedUser);
  }

  @Test
  @TestSecurity(user = "test-user", roles = { "USER" })
  void testGetUserNotFound() {
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .when().pathParam("id", UUID.randomUUID()).get("/user/{id}")
               .then().statusCode(404);
  }
}