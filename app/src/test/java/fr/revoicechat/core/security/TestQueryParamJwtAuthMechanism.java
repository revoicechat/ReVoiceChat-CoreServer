package fr.revoicechat.core.security;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.quarkus.profile.MultiServerProfile;
import fr.revoicechat.core.security.TestQueryParamJwtAuthMechanism.DevTestProfile;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@CleanDatabase
@TestProfile(DevTestProfile.class)
class TestQueryParamJwtAuthMechanism {

  @Test
  void testWrongJwt() {
    RestAssured.given()
               .accept(MediaType.TEXT_PLAIN)
               .contentType(MediaType.TEXT_PLAIN)
               .when().get("/tests/secured-endpoint?jwt=1234")
               .then()
               .statusCode(401);
  }

  @Test
  void testWrongNo() {
    RestAssured.given()
               .accept(MediaType.TEXT_PLAIN)
               .contentType(MediaType.TEXT_PLAIN)
               .when().get("/tests/secured-endpoint")
               .then()
               .statusCode(401);
  }

  @Test
  void test() {
    String validJwt = RestTestUtils.logNewUser();
    RestAssured.given()
               .accept(MediaType.TEXT_PLAIN)
               .contentType(MediaType.TEXT_PLAIN)
               .when().get("/tests/secured-endpoint?jwt="+validJwt)
               .then()
               .statusCode(200);
  }

  public static class DevTestProfile extends MultiServerProfile {
    @Override
    public String getConfigProfile() {
      return "dev,test";
    }
  }
}