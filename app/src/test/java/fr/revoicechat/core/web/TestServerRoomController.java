package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.room.RoomType;
import fr.revoicechat.core.model.ServerType;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomPresence;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
@CleanDatabase
class TestServerRoomController {

  @Test
  void testUpdate() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    List<RoomRepresentation> rooms = getRooms(token, server);
    assertThat(rooms).hasSize(3);
    CreationRoomRepresentation representation = new CreationRoomRepresentation("test", null);
    var room = rooms.getFirst();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(representation)
               .when().pathParam("id", room.id()).patch("/room/{id}")
               .then().statusCode(200);
    var updatedRoom = getRoom(token, room);
    assertThat(updatedRoom.name()).isEqualTo("test");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", room.id()).delete("/room/{id}")
               .then().statusCode(200);
    rooms = getRooms(token, server);
    assertThat(rooms).hasSize(2);
  }

  @Test
  void testRoomPresence() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    var presence = RestAssured.given()
                              .contentType(MediaType.APPLICATION_JSON)
                              .header("Authorization", "Bearer " + token)
                              .when().pathParam("id", room.id()).get("/room/{id}/user")
                              .then().statusCode(200)
                              .extract().body()
                              .as(RoomPresence.class);
    Assertions.assertThat(presence.id()).isEqualTo(room.id());
    Assertions.assertThat(presence.name()).isEqualTo(room.name());
    Assertions.assertThat(presence.allUser()).hasSize(1);
    Assertions.assertThat(presence.connectedUser()).isEmpty();
  }

  @Test
  void testUpdateSameType() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    CreationRoomRepresentation representation = new CreationRoomRepresentation("test", RoomType.TEXT);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(representation)
               .when().pathParam("id", room.id()).patch("/room/{id}")
               .then().statusCode(200);
  }

  @Test
  void testUpdateTypeTextToVocal() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    CreationRoomRepresentation representation = new CreationRoomRepresentation("test", RoomType.VOICE);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(representation)
               .when().pathParam("id", room.id()).patch("/room/{id}")
               .then().statusCode(400);
  }

  @Test
  void testMessage() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    PageResult<MessageRepresentation> page = getPage(token, room, 0);
    assertThat(page.content()).isEmpty();
    IntStream.range(0, 13).forEach(i -> {
      CreatedMessageRepresentation created = new CreatedMessageRepresentation("message " + i, null, List.of());
      RestAssured.given()
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("Authorization", "Bearer " + token)
                 .body(created)
                 .when().pathParam("id", room.id()).put("/room/{id}/message")
                 .then().statusCode(200);
      await().during(250, TimeUnit.MILLISECONDS);
    });
    PageResult<MessageRepresentation> page1 = getPage(token, room, 0);
    assertThat(page1.pageNumber()).isZero();
    assertThat(page1.totalPages()).isEqualTo(2);
    assertThat(page1.content()).hasSize(10).map(MessageRepresentation::text).containsExactly(
        "message 12",
        "message 11",
        "message 10",
        "message 9",
        "message 8",
        "message 7",
        "message 6",
        "message 5",
        "message 4",
        "message 3"
    );
    PageResult<MessageRepresentation> page2 = getPage(token, room, 1);
    assertThat(page2.pageNumber()).isEqualTo(1);
    assertThat(page2.totalPages()).isEqualTo(2);
    assertThat(page2.content()).hasSize(3).map(MessageRepresentation::text)
                               .containsExactly("message 2", "message 1", "message 0");
  }

  @Test
  void testMessageFullPageRetrieving() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    PageResult<MessageRepresentation> page = getPage(token, room, 0);
    assertThat(page.content()).isEmpty();
    IntStream.range(0, 51).forEach(i -> {
      CreatedMessageRepresentation created = new CreatedMessageRepresentation("message " + (i < 10 ? "0" : "") + i, null, List.of());
      RestAssured.given()
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("Authorization", "Bearer " + token)
                 .body(created)
                 .when().pathParam("id", room.id()).put("/room/{id}/message")
                 .then().statusCode(200);
      await().during(250, TimeUnit.MILLISECONDS);
    });
    PageResult<MessageRepresentation> pageFull = getPage(token, room);
    assertThat(pageFull.pageNumber()).isZero();
    assertThat(pageFull.totalPages()).isEqualTo(2);
    assertThat(pageFull.content())
        .hasSize(50).map(MessageRepresentation::text)
        .containsExactly("message 50",
                         "message 49", "message 48", "message 47", "message 46", "message 45", "message 44", "message 43", "message 42", "message 41", "message 40",
                         "message 39", "message 38", "message 37", "message 36", "message 35", "message 34", "message 33", "message 32", "message 31", "message 30",
                         "message 29", "message 28", "message 27", "message 26", "message 25", "message 24", "message 23", "message 22", "message 21", "message 20",
                         "message 19", "message 18", "message 17", "message 16", "message 15", "message 14", "message 13", "message 12", "message 11", "message 10",
                         "message 09", "message 08", "message 07", "message 06", "message 05", "message 04", "message 03", "message 02", "message 01"
        );
  }

  private static PageResult<MessageRepresentation> getPage(final String token, final RoomRepresentation room, int page) {
    var body = RestAssured.given()
                          .contentType(MediaType.APPLICATION_JSON)
                          .header("Authorization", "Bearer " + token)
                          .when()
                          .queryParam("page", page).queryParam("size", 10)
                          .pathParam("id", room.id()).get("/room/{id}/message")
                          .then().statusCode(200)
                          .extract().body();
    var pageResult = body.as(PageResult.class);
    var messages = body.jsonPath().getList("content", MessageRepresentation.class);
    return new PageResult<>(messages, pageResult.pageNumber(), pageResult.pageSize(), pageResult.totalElements());
  }

  private static PageResult<MessageRepresentation> getPage(final String token, final RoomRepresentation room) {
    var body = RestAssured.given()
                          .contentType(MediaType.APPLICATION_JSON)
                          .header("Authorization", "Bearer " + token)
                          .when()
                          .pathParam("id", room.id()).get("/room/{id}/message")
                          .then().statusCode(200)
                          .extract().body();
    var pageResult = body.as(PageResult.class);
    var messages = body.jsonPath().getList("content", MessageRepresentation.class);
    return new PageResult<>(messages, pageResult.pageNumber(), pageResult.pageSize(), pageResult.totalElements());
  }

  private static RoomRepresentation createRoom(final String token, final ServerRepresentation server) {
    CreationRoomRepresentation representation = new CreationRoomRepresentation("test", RoomType.TEXT);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(representation)
                      .when().pathParam("id", server.id()).put("/server/{id}/room")
                      .then().statusCode(200)
                      .extract().body().as(RoomRepresentation.class);
  }

  private static RoomRepresentation getRoom(final String token, final RoomRepresentation room) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().pathParam("id", room.id()).get("/room/{id}")
                      .then().statusCode(200)
                      .extract().as(RoomRepresentation.class);
  }

  private static List<RoomRepresentation> getRooms(final String token, final ServerRepresentation server) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().pathParam("id", server.id()).get("/server/{id}/room")
                      .then().statusCode(200)
                      .extract().jsonPath().getList(".", RoomRepresentation.class);
  }

  private static ServerRepresentation createServer(String token) {
    var representation = new ServerCreationRepresentation("test", ServerType.PUBLIC);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(representation)
                      .when().put("/server")
                      .then().statusCode(200)
                      .extract().as(ServerRepresentation.class);
  }
}