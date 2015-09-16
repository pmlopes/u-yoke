package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Yoke;
import io.u.yoke.http.header.Headers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static io.u.yoke.test.Yoke.yoke;
import static org.hamcrest.Matchers.notNullValue;

public class BasicAuthTest {

  private Yoke app;

  @Before
  public void setUp() throws Exception {
    app = yoke();
  }

  @After
  public void tearDown() {
    // close yoke
    app.clear();
  }

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
