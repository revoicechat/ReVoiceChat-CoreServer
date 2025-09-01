package fr.revoicechat.core.web.tests;

import java.util.UUID;
import jakarta.ws.rs.core.MediaType;

import fr.revoicechat.core.representation.user.SignupRepresentation;
import io.restassured.RestAssured;

public class RestTestUtils {

  public static void signup(String user, String password) {
    var signup = new SignupRepresentation(user, password, "nyphew@mail.com", UUID.randomUUID());
    RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .body(signup)
               .when().put("/auth/signup")
               .then().statusCode(200);
  }
}
