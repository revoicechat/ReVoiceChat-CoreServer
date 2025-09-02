package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.representation.invitation.InvitationRepresentation;
import fr.revoicechat.core.web.tests.RestTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(BasicIntegrationTestProfile.class)
@CleanDatabase
class TestInvitationLinkController {

  @Test
  void testGenerateApplicationInvitation() {
    String token = RestTestUtils.logNewUser();
    var invitationCreated = generateApplicationInvitation(token);
    assertThat(invitationCreated).isNotNull();
    assertThat(invitationCreated.id()).isNotNull();
    assertThat(invitationCreated.type()).isEqualTo(InvitationType.APPLICATION_JOIN);
    assertThat(invitationCreated.status()).isEqualTo(InvitationLinkStatus.CREATED);
  }

  private static InvitationRepresentation generateApplicationInvitation(final String token) {
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .header("Authorization", "Bearer " + token)
                      .when().post("/invitation/application")
                      .then().statusCode(200)
                      .extract().as(InvitationRepresentation.class);
  }

  @Test
  void testRevoke() {
    String token = RestTestUtils.logNewUser();
    var invitationCreated = generateApplicationInvitation(token);
    assertThat(invitationCreated.status()).isEqualTo(InvitationLinkStatus.CREATED);
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", invitationCreated.id()).delete("/invitation/{id}")
               .then().statusCode(204);
    var invitationDeleted = RestAssured.given()
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .header("Authorization", "Bearer " + token)
                                       .when().pathParam("id", invitationCreated.id()).get("/invitation/{id}")
                                       .then().statusCode(200)
                                       .extract().as(InvitationRepresentation.class);
    assertThat(invitationDeleted.id()).isEqualTo(invitationCreated.id());
    assertThat(invitationDeleted.status()).isEqualTo(InvitationLinkStatus.REVOKED);
  }

  @Test
  void testGetInvitationNoResource() {
    String token = RestTestUtils.logNewUser();
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .header("Authorization", "Bearer " + token)
               .when().pathParam("id", UUID.randomUUID()).get("/invitation/{id}")
               .then().statusCode(404);
  }
}