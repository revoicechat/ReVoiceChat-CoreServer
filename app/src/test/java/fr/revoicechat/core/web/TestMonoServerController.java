package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.nls.ServerErrorCode;
import fr.revoicechat.core.quarkus.profile.MonoServerProfile;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import fr.revoicechat.web.nls.HttpStatusErrorCode;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(MonoServerProfile.class)
@CleanDatabase
class TestMonoServerController {

  @Test
  void creationIsImpossible() {
    String token = RestTestUtils.logNewUser();
    var representation = new ServerCreationRepresentation("test");
    var response = RestAssured.given()
                              .contentType(MediaType.APPLICATION_JSON)
                              .header("Authorization", "Bearer " + token)
                              .body(representation)
                              .when().put("/server");
    assertThat(response.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    assertThat(response.asString()).isEqualTo(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION.translate());
  }

  @Test
  void testGetServers() {
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    assertThat(server).isNotNull();
    assertThat(server.id()).isNotNull();
    assertThat(server.name()).isEqualTo("Server");
    assertThat(server.owner()).isNotNull();
  }

  @Test
  void testGetServerExists() {
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    ServerRepresentation serv = RestAssured.given()
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .header("Authorization", "Bearer " + token)
                                           .when().pathParam("id", server.id()).get("/server/{id}")
                                           .then().statusCode(200)
                                           .extract()
                                           .body().as(ServerRepresentation.class);
    assertThat(serv).isEqualTo(server);
  }

  @Test
  void testGetServerNotExists() {
    String token = RestTestUtils.logNewUser();
    var randomId = UUID.randomUUID();
    var error = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .header("Authorization", "Bearer " + token)
                           .when().pathParam("id", randomId).get("/server/{id}")
                           .then().statusCode(404)
                           .extract()
                           .body().asString();
    assertThat(error).isEqualTo(HttpStatusErrorCode.NOT_FOUND.translate(Server.class.getSimpleName(), randomId));
  }

  @Test
  void testGetRooms() {
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    var room = getRooms(server.id(), token);
    assertThat(room).hasSize(3);
  }

  @Test
  void testCreateRooms() {
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new CreationRoomRepresentation("room", RoomType.TEXT))
               .when().pathParam("id", server.id()).put("/server/{id}/room")
               .then().statusCode(200);
    var room = getRooms(server.id(), token);
    assertThat(room).hasSize(4);
  }

  @Test
  void testDeleteServer() {
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", servers.getFirst().id()).delete("/server/{id}")
               .then().statusCode(400).body(is(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_DELETION.translate()));
  }

  @Test
  void fetchUser() {
    RestTestUtils.signup("Nyphew", "a");
    String token = RestTestUtils.logNewUser();
    var servers = getServers(token);
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    var users = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .header("Authorization", "Bearer " + token)
                           .when().pathParam("id", server.id()).get("/server/{id}/user")
                           .then().statusCode(200)
                           .extract()
                           .body().jsonPath().getList(".", UserRepresentation.class);
    assertThat(users).hasSize(2);
  }

  private static List<ServerRepresentation> getServers(String token) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().get("/server")
                      .then().statusCode(200)
                      .extract()
                      .body()
                      .jsonPath().getList(".", ServerRepresentation.class);
  }

  private static List<RoomRepresentation> getRooms(UUID id, String token) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().pathParam("id", id).get("/server/{id}/room")
                      .then().statusCode(200)
                      .extract()
                      .body()
                      .jsonPath().getList(".", RoomRepresentation.class);
  }
}