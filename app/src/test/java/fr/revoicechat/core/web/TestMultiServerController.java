package fr.revoicechat.core.web;

import static fr.revoicechat.core.web.tests.RestTestUtils.signup;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.DBCleaner;
import fr.revoicechat.core.junit.UserCreator;
import fr.revoicechat.core.quarkus.profile.H2Profile;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.web.TestMultiServerController.MultiServerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(MultiServerProfile.class)
@TestSecurity(authorizationEnabled = false)
class TestMultiServerController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;
  @Inject UserHolder holder;

  @BeforeEach
  void setUp() {
    cleaner.clean();
    creator.create();
  }

  @Test
  void creationPossible() {
    var server = createServer();
    assertThat(server).isNotNull();
    assertThat(server.id()).isNotNull();
    assertThat(server.name()).isEqualTo("test");
    assertThat(server.owner()).isEqualTo(holder.get().getId());
  }

  @Test
  void testUpdateServer() {
    createServer();
    var servers = getServers();
    ServerRepresentation server = servers.getFirst();
    assertThat(server.name()).isEqualTo("test");
    var newName = new ServerCreationRepresentation("new name");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .body(newName)
               .when().pathParam("id", server.id()).patch("/server/{id}")
               .then().statusCode(200);
    server = RestAssured.given()
                        .contentType(MediaType.APPLICATION_JSON)
                        .when().pathParam("id", server.id()).get("/server/{id}")
                        .then().statusCode(200)
                        .extract().as(ServerRepresentation.class);
    assertThat(server.name()).isEqualTo("new name");
  }

  @Test
  void testUpdateServerButResourceNotFound() {
    var newName = new ServerCreationRepresentation("new name");
    var randomId = UUID.randomUUID();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .body(newName)
               .when().pathParam("id", randomId).patch("/server/{id}")
               .then().statusCode(404);
  }

  @Test
  @Transactional
  void testGetServer() {
    assertThat(getServers()).isEmpty();
    createServer("test1");
    createServer("test2");
    var servers = getServers();
    assertThat(servers).hasSize(2);
  }

  @Test
  void fetchUser() {
    signup("Nyphew", "a");
    var server = createServer();
    var users = RestAssured.given()
                           .contentType(MediaType.APPLICATION_JSON)
                           .when().pathParam("id", server.id()).get("/server/{id}/user")
                           .then().statusCode(200)
                           .extract()
                           .body().jsonPath().getList(".", UserRepresentation.class);
    assertThat(users).hasSize(1);
  }

  private static ServerRepresentation createServer() {
    return createServer("test");
  }

  private static ServerRepresentation createServer(String name) {
    var representation = new ServerCreationRepresentation(name);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(representation)
                      .when().put("/server")
                      .then().statusCode(200)
                      .extract().as(ServerRepresentation.class);
  }

  private static List<ServerRepresentation> getServers() {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .when().get("/server")
                      .then().statusCode(200)
                      .extract()
                      .body().jsonPath().getList(".", ServerRepresentation.class);
  }

  public static class MultiServerProfile extends H2Profile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      config.put("revoicechat.global.sever-mode", "MULTI_SERVER");
      return config;
    }
  }
}