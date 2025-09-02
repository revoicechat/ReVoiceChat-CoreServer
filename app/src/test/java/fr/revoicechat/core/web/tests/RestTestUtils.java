package fr.revoicechat.core.web.tests;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.ws.rs.core.MediaType;

import fr.revoicechat.core.representation.login.UserPassword;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import io.restassured.RestAssured;

public class RestTestUtils {

  private static final AtomicInteger counter = new AtomicInteger(1);

  public static String logNewUser() {
    RestTestUtils.signup("user", "psw");
    return RestTestUtils.login("user", "psw");
  }

  public static UserRepresentation signup(String user, String password) {
    var signup = new SignupRepresentation(user, password, counter.getAndIncrement() + "@mail.com", UUID.randomUUID());
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(signup)
                      .when().put("/auth/signup")
                      .then().statusCode(200)
                      .extract().body().as(UserRepresentation.class);
  }

  public static String login(String user, String password) {
    var login = new UserPassword(user, password);
    return RestAssured.given()
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(login)
                      .when().post("/auth/login")
                      .then().statusCode(200)
                      .extract().body().asString();
  }
}
