package fr.revoicechat.stub.risk.web;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.quarkus.profile.MultiServerProfile;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import fr.revoicechat.risk.model.RiskMode;
import fr.revoicechat.risk.representation.CreatedServerRoleRepresentation;
import fr.revoicechat.risk.representation.RiskRepresentation;
import fr.revoicechat.risk.representation.ServerRoleRepresentation;
import fr.revoicechat.core.risk.RoomRiskType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@CleanDatabase
@TestProfile(MultiServerProfile.class)
class TestServerRoleController {

  @Test
  void test() {
    var adminToken = RestTestUtils.logNewUser("admin");
    var userToken = RestTestUtils.logNewUser("user");
    var server = createServer(adminToken);
    CreatedServerRoleRepresentation serverRole = new CreatedServerRoleRepresentation(
        "test",
        null,
        1,
        List.of(new RiskRepresentation(RoomRiskType.SERVER_ROOM_READ_MESSAGE, null, RiskMode.ENABLE))
    );
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + userToken)
               .body(serverRole)
               .when().pathParam("id", server.id()).put("/server/{id}/role")
               .then().statusCode(401);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + adminToken)
               .body(serverRole)
               .when().pathParam("id", server.id()).put("/server/{id}/role")
               .then().statusCode(200);
    var result = RestAssured.given()
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .body(serverRole)
                            .when().pathParam("id", server.id()).get("/server/{id}/role")
                            .then().statusCode(200)
                            .extract().jsonPath().getList(".", ServerRoleRepresentation.class);
    Assertions.assertThat(result).hasSize(2);
  }

  private static ServerRepresentation createServer(String token) {
    var representation = new ServerCreationRepresentation("test");
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(representation)
                      .when().put("/server")
                      .then().statusCode(200)
                      .extract().as(ServerRepresentation.class);
  }
}