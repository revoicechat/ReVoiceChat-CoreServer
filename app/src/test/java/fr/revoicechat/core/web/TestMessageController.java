package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.FileType;
import fr.revoicechat.core.model.MediaData;
import fr.revoicechat.core.model.MediaDataStatus;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.ServerType;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.repository.page.PageResult;
import fr.revoicechat.core.representation.media.CreatedMediaDataRepresentation;
import fr.revoicechat.core.representation.message.CreatedMessageRepresentation;
import fr.revoicechat.core.representation.message.MessageRepresentation;
import fr.revoicechat.core.representation.room.CreationRoomRepresentation;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@CleanDatabase
@TestProfile(BasicIntegrationTestProfile.class)
class TestMessageController {

  @Inject EntityManager entityManager;

  @Test
  @Transactional
  void testGetMessage() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    var messageId = createMessage(token, room).id();
    var message = RestAssured.given()
                             .contentType(MediaType.APPLICATION_JSON)
                             .header("Authorization", "Bearer " + token)
                             .when().pathParam("id", messageId).get("/message/{id}")
                             .then().statusCode(200)
                             .extract().body().as(MessageRepresentation.class);
    assertThat(message.text()).isEqualTo("message 1");
    assertThat(message.user()).isNotNull();
    assertThat(message.medias()).hasSize(2)
                                .anyMatch(media -> media.name().equals("test1.png") && media.type().equals(FileType.PICTURE))
                                .anyMatch(media -> media.name().equals("test2.mp4") && media.type().equals(FileType.VIDEO));
    for (var media : message.medias()) {
      var entity = entityManager.find(MediaData.class, media.id());
      assertThat(entity).isNotNull();
      assertThat(entity.getStatus()).isEqualTo(MediaDataStatus.DOWNLOADING);
    }
  }

  @Test
  void testUpdateMessage() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    var message = createMessage(token, room);
    PageResult<MessageRepresentation> page1 = getPage(token, room);
    assertThat(page1.content()).hasSize(1).map(MessageRepresentation::text).containsExactly("message 1");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new CreatedMessageRepresentation("message 2", null, List.of()))
               .when().pathParam("id", message.id()).patch("/message/{id}")
               .then().statusCode(200);
    PageResult<MessageRepresentation> page2 = getPage(token, room);
    assertThat(page2.content()).hasSize(1).map(MessageRepresentation::text).containsExactly("message 2");
  }

  @Test
  void testDeleteMessage() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    var message = createMessage(token, room);
    PageResult<MessageRepresentation> page1 = getPage(token, room);
    assertThat(page1.content()).hasSize(1);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", message.id()).delete("/message/{id}")
               .then().statusCode(200);
    PageResult<MessageRepresentation> page3 = getPage(token, room);
    assertThat(page3.content()).isEmpty();
  }

  @Test
  void testWithReactions() {
    String token = RestTestUtils.logNewUser();
    var server = createServer(token);
    var room = createRoom(token, server);
    var message = createMessage(token, room);
    PageResult<MessageRepresentation> page1 = getPage(token, room);
    assertThat(page1.content()).hasSize(1).map(MessageRepresentation::text).containsExactly("message 1");
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new CreatedMessageRepresentation("message 2", null, List.of()))
               .when()
               .pathParam("id", message.id())
               .pathParam("emoji", "👽")
               .put("/message/{id}/reaction/{emoji}")
               .then()
               .statusCode(200);
    PageResult<MessageRepresentation> page2 = getPage(token, room);
    assertThat(page2.content()).hasSize(1);
    message = page2.content().getFirst();
    assertThat(message.reactions()).hasSize(1);
    assertThat(message.reactions().getFirst().emoji()).isEqualTo("👽");
    assertThat(message.reactions().getFirst().users()).hasSize(1);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .body(new CreatedMessageRepresentation("message 2", null, List.of()))
               .when()
               .pathParam("id", message.id())
               .pathParam("emoji", "👽")
               .delete("/message/{id}/reaction/{emoji}")
               .then()
               .statusCode(200);
    PageResult<MessageRepresentation> page3 = getPage(token, room);
    assertThat(page3.content()).hasSize(1);
    message = page3.content().getFirst();
    assertThat(message.reactions()).isEmpty();
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

  private static MessageRepresentation createMessage(final String token, final RoomRepresentation room) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(new CreatedMessageRepresentation("message 1", null, List.of(new CreatedMediaDataRepresentation("test1.png"),
                                                                                  new CreatedMediaDataRepresentation("test2.mp4"))))
                      .when().pathParam("id", room.id()).put("/room/{id}/message")
                      .then().statusCode(200)
                      .extract().body().as(MessageRepresentation.class);
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