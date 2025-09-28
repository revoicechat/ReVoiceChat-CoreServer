package fr.revoicechat.risk.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

/** @see fr.revoicechat.risk.web.api.RiskController */
@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
@CleanDatabase
class TestRiskController {

  @Test
  void test() {
    var response = RestAssured.given()
                              .contentType(MediaType.APPLICATION_JSON)
                              .when().get("/risk")
                              .then().statusCode(200)
                              .extract().body().asPrettyString();
    assertThat(response).contains("\"type\": \"SERVER_RISK_TYPE\"", "\"title\": \"risk associated to server\",");
    assertThat(response).contains("\"type\": \"SERVER_ROOM_READ_MESSAGE\"", "\"title\": \"read messages on a specific room\"");
  }
}