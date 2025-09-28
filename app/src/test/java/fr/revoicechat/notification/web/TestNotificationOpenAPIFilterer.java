package fr.revoicechat.notification.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
class TestNotificationOpenAPIFilterer {

  @Test
  void testNotificationPayloads() {
    String json = RestAssured.get("/q/openapi?format=json").asPrettyString();
    JsonPath path = new JsonPath(json);

    List<String> type = path.getList("paths.'/api/sse'.get.responses.200.content.\"application/json\".schema.properties.type.enum");
    List<String> data = path.getList("paths.'/api/sse'.get.responses.200.content.\"application/json\".schema.properties.data.oneOf.$ref");
    assertThat(type).hasSameSizeAs(data);
    assertThat(data).contains("#/components/schemas/MessageNotification", "#/components/schemas/Ping");
    assertThat(type).contains("ROOM_MESSAGE", "PING");
  }
}