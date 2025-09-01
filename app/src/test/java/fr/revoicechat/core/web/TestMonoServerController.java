package fr.revoicechat.core.web;

import static fr.revoicechat.core.web.tests.RestTestUtils.signup;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.DBCleaner;
import fr.revoicechat.core.junit.UserCreator;
import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.nls.CommonErrorCode;
import fr.revoicechat.core.nls.ServerErrorCode;
import fr.revoicechat.core.quarkus.profile.H2Profile;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.web.TestMonoServerController.MonoServerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(MonoServerProfile.class)
@TestSecurity(authorizationEnabled = false)
class TestMonoServerController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;

  @BeforeEach
  void setUp() {
    cleaner.clean();
    creator.create();
  }

  @Test
  void creationIsImpossible() {
    var representation = new ServerCreationRepresentation("test");
    var response = RestAssured.given()
                              .contentType(MediaType.APPLICATION_JSON)
                              .body(representation)
                              .when().put("/server");
    assertThat(response.statusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    assertThat(response.asString()).isEqualTo(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION.translate());
  }

  @Test
  void testGetServers() {
    var servers = getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    assertThat(server).isNotNull();
    assertThat(server.id()).isNotNull();
    assertThat(server.name()).isEqualTo("Server");
    assertThat(server.owner()).isNull();
  }

  @Test
  void testGetServerExists() {
    var servers = getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    ServerRepresentation serv = RestAssured.given()
                             .contentType(MediaType.APPLICATION_JSON)
                             .when().pathParam("id", server.id()).get("/server/{id}")
                             .then().statusCode(200)
                             .extract()
                             .body().as(ServerRepresentation.class);
    assertThat(serv).isEqualTo(server);
  }

  @Test
  void testGetServerNotExists() {
    var randomId = UUID.randomUUID();
    var error = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .when().pathParam("id", randomId).get("/server/{id}")
                           .then().statusCode(404)
                           .extract()
                           .body().asString();
    assertThat(error).isEqualTo(CommonErrorCode.NOT_FOUND.translate(Server.class.getSimpleName(), randomId));
  }

  @Test
  void testGetRooms() {
    var servers = getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    var room = getRooms(server.id());
    assertThat(room).hasSize(3);
  }

  @Test
  void testCreateRooms() {
    var servers = getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .body(new RoomRepresentation("room", RoomType.TEXT))
               .when().pathParam("id", server.id()).put("/server/{id}/room")
               .then().statusCode(200);
    var room = getRooms(server.id());
    assertThat(room).hasSize(4);
  }

  @Test
  void fetchUser() {
    signup("Nyphew", "a");
    var servers = getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    var users = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .when().pathParam("id", server.id()).get("/server/{id}/user")
                           .then().statusCode(200)
                           .extract()
                           .body().jsonPath().getList(".", UserRepresentation.class);
    assertThat(users).hasSize(2);
  }

  private static List<ServerRepresentation> getServers() {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .when().get("/server")
                      .then().statusCode(200)
                      .extract()
                      .body()
                      .jsonPath().getList(".", ServerRepresentation.class);
  }

  private static List<Room> getRooms(UUID id) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .when().pathParam("id", id).get("/server/{id}/room")
                      .then().statusCode(200)
                      .extract()
                      .body()
                      .jsonPath().getList(".", Room.class);
  }

  public static class MonoServerProfile extends H2Profile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      config.put("revoicechat.global.sever-mode", "MONO_SERVER");
      return config;
    }
  }
}