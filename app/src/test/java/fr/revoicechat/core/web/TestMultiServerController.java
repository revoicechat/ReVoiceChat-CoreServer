package fr.revoicechat.core.web;

import static fr.revoicechat.core.web.tests.RestTestUtils.signup;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.quarkus.profile.MultiServerProfile;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(MultiServerProfile.class)
@CleanDatabase
class TestMultiServerController {

  @Test
  void creationPossible() {
    var user = RestTestUtils.signup("user", "psw");
    String token = RestTestUtils.login("user", "psw");
    var server = createServer(token);
    assertThat(server).isNotNull();
    assertThat(server.id()).isNotNull();
    assertThat(server.name()).isEqualTo("test");
    assertThat(server.owner()).isEqualTo(user.id());
  }

  @Test
  void testUpdateServer() {
    String token = RestTestUtils.logNewUser();
    createServer(token);
    var servers = getServers(token);
    ServerRepresentation server = servers.getFirst();
    assertThat(server.name()).isEqualTo("test");
    var newName = new ServerCreationRepresentation("new name");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(newName)
               .when().pathParam("id", server.id()).patch("/server/{id}")
               .then().statusCode(200);
    server = RestAssured.given()
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .when().pathParam("id", server.id()).get("/server/{id}")
                        .then().statusCode(200)
                        .extract().as(ServerRepresentation.class);
    assertThat(server.name()).isEqualTo("new name");
  }

  @Test
  void testUpdateServerByAnotherUser() {
    String tokenAdmin = RestTestUtils.logNewUser("admin");
    String tokenUser1 = RestTestUtils.logNewUser("user_1");
    String tokenUser2 = RestTestUtils.logNewUser("user_2");
    createServer(tokenUser1);
    var servers = getServers(tokenUser1);
    ServerRepresentation server = servers.getFirst();
    assertThat(server.name()).isEqualTo("test");
    var newName = new ServerCreationRepresentation("new name");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + tokenAdmin)
               .body(newName)
               .when().pathParam("id", server.id()).patch("/server/{id}")
               .then().statusCode(200);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + tokenUser1)
               .body(newName)
               .when().pathParam("id", server.id()).patch("/server/{id}")
               .then().statusCode(200);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + tokenUser2)
               .body(newName)
               .when().pathParam("id", server.id()).patch("/server/{id}")
               .then().statusCode(401);
  }

  @Test
  void testUpdateServerButResourceNotFound() {
    String token = RestTestUtils.logNewUser();
    var newName = new ServerCreationRepresentation("new name");
    var randomId = UUID.randomUUID();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(newName)
               .when().pathParam("id", randomId).patch("/server/{id}")
               .then().statusCode(404);
  }

  @Test
  void testGetServer() {
    String token = RestTestUtils.logNewUser();
    assertThat(getServers(token)).isEmpty();
    createServer(token, "test1");
    createServer(token, "test2");
    var servers = getServers(token);
    assertThat(servers).hasSize(2);
  }

  @Test
  void testDeleteServer() {
    String token = RestTestUtils.logNewUser();
    var server1 = createServer(token, "test1");
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", server1.id()).delete("/server/{id}")
               .then().statusCode(204);
    servers = getServers(token);
    assertThat(servers).isEmpty();
  }

  @Test
  void fetchUser() {
    signup("Nyphew", "a");
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var users = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .header("Authorization", "Bearer " + token)
                           .when().pathParam("id", server.id()).get("/server/{id}/user")
                           .then().statusCode(200)
                           .extract()
                           .body().jsonPath().getList(".", UserRepresentation.class);
    assertThat(users).hasSize(1);
  }

  private static ServerRepresentation createServer(String token) {
    return createServer(token, "test");
  }

  private static ServerRepresentation createServer(String token, String name) {
    var representation = new ServerCreationRepresentation(name);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(representation)
                      .when().put("/server")
                      .then().statusCode(200)
                      .extract().as(ServerRepresentation.class);
  }

  private static List<ServerRepresentation> getServers(String token) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().get("/server")
                      .then().statusCode(200)
                      .extract()
                      .body().jsonPath().getList(".", ServerRepresentation.class);
  }
}