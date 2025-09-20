package fr.revoicechat.core.web;

import static fr.revoicechat.core.web.tests.RestTestUtils.signup;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.model.server.ServerCategory;
import fr.revoicechat.core.model.server.ServerRoom;
import fr.revoicechat.core.model.server.ServerStructure;
import fr.revoicechat.core.quarkus.profile.MultiServerProfile;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
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

  @Test
  void testStructure() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var structure = RestAssured.given()
                               .contentType(MediaType.APPLICATION_JSON)
                               .header("Authorization", "Bearer " + token)
                               .when().pathParam("id", server.id()).get("/server/{id}/structure")
                               .then().statusCode(200)
                               .extract()
                               .body().as(ServerStructure.class);
    assertThat(structure.items()).hasSize(2);
    var item1 = (ServerCategory) structure.items().getFirst();
    assertThat(item1.name()).isEqualTo("text");
    assertThat(item1.items()).hasSize(2);
    var room1 = (ServerRoom) item1.items().getFirst();
    assertThat(getRoom(token, room1.id()).name()).isEqualTo("General");
    var room2 = (ServerRoom) item1.items().getLast();
    assertThat(getRoom(token, room2.id()).name()).isEqualTo("Random");
    assertThat(item1.items()).hasSize(2);
    var item2 = (ServerCategory) structure.items().getLast();
    assertThat(item2.name()).isEqualTo("vocal");
    assertThat(item2.items()).hasSize(1);
    var room3 = (ServerRoom) item2.items().getFirst();
    assertThat(getRoom(token, room3.id()).name()).isEqualTo("Vocal");
  }

  @Test
  void testUpdateEmptyStructure() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new ServerStructure(List.of()))
               .when().pathParam("id", server.id()).patch("/server/{id}/structure")
               .then().statusCode(200);
    var structure = RestAssured.given()
                               .contentType(MediaType.APPLICATION_JSON)
                               .header("Authorization", "Bearer " + token)
                               .when().pathParam("id", server.id()).get("/server/{id}/structure")
                               .then().statusCode(200)
                               .extract()
                               .body().as(ServerStructure.class);
    assertThat(structure.items()).isEmpty();
  }

  @Test
  void testUpdateStructure() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var structureBefore = RestAssured.given()
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .header("Authorization", "Bearer " + token)
                                     .when().pathParam("id", server.id()).get("/server/{id}/structure")
                                     .then().statusCode(200)
                                     .extract()
                                     .body().as(ServerStructure.class);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new ServerStructure(List.of(structureBefore.items().getFirst())))
               .when().pathParam("id", server.id()).patch("/server/{id}/structure")
               .then().statusCode(200);
    var structure = RestAssured.given()
                               .contentType(MediaType.APPLICATION_JSON)
                               .header("Authorization", "Bearer " + token)
                               .when().pathParam("id", server.id()).get("/server/{id}/structure")
                               .then().statusCode(200)
                               .extract()
                               .body().as(ServerStructure.class);
    assertThat(structure.items()).hasSize(1);
  }

  @Test
  void testUpdateStructureWithInexistantRoomId() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new ServerStructure(List.of(new ServerRoom(UUID.randomUUID()))))
               .when().pathParam("id", server.id()).patch("/server/{id}/structure")
               .then().statusCode(400);
  }

  @Test
  void testRemoveStructure() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", server.id()).patch("/server/{id}/structure")
               .then().statusCode(200);
    var structure = RestAssured.given()
                               .contentType(MediaType.APPLICATION_JSON)
                               .header("Authorization", "Bearer " + token)
                               .when().pathParam("id", server.id()).get("/server/{id}/structure")
                               .then().statusCode(200)
                               .extract()
                               .body().as(ServerStructure.class);
    assertThat(structure.items()).hasSize(3).allMatch(ServerRoom.class::isInstance);
  }

  @Test
  void testInvitationServer() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var invitation = RestAssured.given()
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .when().pathParam("id", server.id()).post("/server/{id}/invitation")
                                .then().statusCode(200)
                                .extract().body().as(InvitationRepresentation.class);
    assertThat(invitation.id()).isNotNull();
    assertThat(invitation.status()).isEqualTo(InvitationLinkStatus.CREATED);
    assertThat(invitation.type()).isEqualTo(InvitationType.SERVER_JOIN);
    assertThat(invitation.targetedServer()).isEqualTo(server.id());
  }

  @Test
  void testGetServerInvitation() {
    String tokenAdmin = RestTestUtils.logNewUser("admin");
    String tokenUser = RestTestUtils.logNewUser("user");
    var server = createServer(tokenAdmin);
    serverInvitation(tokenUser, server);
    serverInvitation(tokenUser, server);
    applicationInvitation(tokenUser);
    applicationInvitation(tokenUser);
    serverInvitation(tokenAdmin, server);
    applicationInvitation(tokenAdmin);
    applicationInvitation(tokenAdmin);
    var appInvitations = RestAssured.given()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + tokenAdmin)
                                    .when().pathParam("id", server.id()).get("/server/{id}/invitation")
                                    .then().statusCode(200)
                                    .extract().body().jsonPath().getList(".", InvitationRepresentation.class);
    assertThat(appInvitations).hasSize(3);
  }

  private static void serverInvitation(final String tokenUser, final ServerRepresentation server) {
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + tokenUser)
               .when().pathParam("id", server.id()).post("/server/{id}/invitation")
               .then().statusCode(200);
  }

  private static void applicationInvitation(final String tokenUser) {
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + tokenUser)
               .when().post("/invitation/application")
               .then().statusCode(200);
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

  private RoomRepresentation getRoom(String token, UUID id) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().pathParam("id", id).get("/room/{id}")
                      .then().statusCode(200)
                      .extract()
                      .body().as(RoomRepresentation.class);
  }
}