package fr.revoicechat.core.dev;

import jakarta.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.dev.TestDevOnlyController.DevTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(DevTestProfile.class)
class TestDevOnlyController {

  @Test
  void testError() {
    var result = RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                            .when().get("/error/throw")
                            .then().statusCode(500)
                            .extract().body().asPrettyString();
    Assertions.assertThat(result).isEqualToNormalizingNewlines("""
                                                                   {
                                                                       "error": "Internal Server Error",
                                                                       "message": "Something went wrong on our side. Please try again later or contact support if the problem persists.",
                                                                       "errorFile": "unable to generate an internal error file",
                                                                       "swaggerDoc": "/api/q/swagger-ui"
                                                                   }""");
  }

  @Test
  void testErrorInFrench() {
    var result = RestAssured.given()
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Accept-Language", "fr")
                            .when().get("/error/throw")
                            .then().statusCode(500)
                            .extract().body().asPrettyString();
    Assertions.assertThat(result).isEqualToNormalizingNewlines("""
                                                                   {
                                                                       "error": "Erreur interne du serveur",
                                                                       "message": "Problème est survenu de notre côté. Veuillez réessayer plus tard ou contacter lassistance si le problème persiste.",
                                                                       "errorFile": "unable to generate an internal error file",
                                                                       "swaggerDoc": "/api/q/swagger-ui"
                                                                   }""");
  }

  public static class DevTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() {
      return "dev,test";
    }
  }
}