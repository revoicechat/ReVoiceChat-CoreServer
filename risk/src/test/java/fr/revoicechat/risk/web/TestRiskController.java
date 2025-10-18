package fr.revoicechat.risk.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.revoicechat.risk.type.RiskCategory;
import fr.revoicechat.risk.type.RiskType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

/** @see fr.revoicechat.risk.web.api.RiskController */
@QuarkusTest
class TestRiskController {

  @Test
  void test() {
    var response = RestAssured.given()
                              .contentType(MediaType.APPLICATION_JSON)
                              .when().get("/risk")
                              .then().statusCode(200)
                              .extract().body().asPrettyString();
    assertThat(response).contains("\"type\": \"RISK_TYPE_MOCK\"",
        "\"title\": \"risk type mock\",",
        "\"type\": \"MOCK_RISK_1\"",
        "\"title\": \"risk 1\"",
        "\"type\": \"MOCK_RISK_2\"",
        "\"title\": \"risk 2\"")
        .doesNotContain("\"type\": \"NO_RISK_TYPE_MOCK\"");
  }

  @RiskCategory("NO_RISK_TYPE_MOCK")
  @SuppressWarnings("unused") // here for reflection test purpose
  public enum NoRiskTypeMock implements RiskType {
    ;

    @Override
    public String fileName() {
      return "fr.revoicechat.risk.type.RiskTypeMock";
    }
  }

  @RiskCategory("RISK_TYPE_MOCK")
  @SuppressWarnings("unused") // here for reflection test purpose
  public enum RiskTypeMock implements RiskType {
    MOCK_RISK_1,
    MOCK_RISK_2,
    ;

    @Override
    public String fileName() {
      return "fr.revoicechat.risk.type.RiskTypeMock";
    }
  }
}