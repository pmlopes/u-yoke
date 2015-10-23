package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.base.AbstractIT;
import io.u.yoke.http.header.Headers;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class BasicAuthIT extends AbstractIT {

  @Test
  public void testBasicAuth() {
    app.use(new BasicAuth("Aladdin", "open sesame"));
    app.use(Context::end);

    // first time is forbidden
    get("/").then()
        .statusCode(401)
        .header(Headers.WWW_AUTHENTICATE, notNullValue());

    // second time send the authorization header
    given()
        .header(Headers.AUTHORIZATION, "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")
    .when()
        .get("/").then()
        .statusCode(200);
  }

  @Test
  public void testEmptyPassword() {
    app.use(new BasicAuth((username, password, cb) -> {
      boolean success = username.equals("Aladdin") && password == null;
      if (success) {
        cb.call(username);
      } else {
        cb.call(new RuntimeException("Bad Credentials"), null);
      }
    }));

    app.use(Context::end);

    // first time is forbidden
    get("/").then()
        .statusCode(401)
        .header(Headers.WWW_AUTHENTICATE, notNullValue());

    // second time send the authorization header
    given()
        .header(Headers.AUTHORIZATION, "Basic QWxhZGRpbjo=")
    .when()
        .get("/").then()
        .statusCode(200);
  }
}
