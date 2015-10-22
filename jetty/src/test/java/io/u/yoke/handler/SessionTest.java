package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Yoke;
import io.u.yoke.http.header.Headers;
import io.u.yoke.security.Security;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;

import static com.jayway.restassured.RestAssured.*;
import static io.u.yoke.test.Yoke.yoke;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

public class SessionTest {

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
  public void testSession() {

    final Security security = Security.create("keyboard cat");
    final Mac hmac = security.getMac("HmacSHA256");

    app.use(new CookieParser(hmac));
    app.use(new Session());

    app.use(new Router()
            .get("/", Context::end)
            .get("/new", ctx -> {
              ctx.createSession();
              ctx.end();
            })
            .get("/delete", ctx -> {
              ctx.destroySession();
              ctx.end();
            })
    );

    // start: there is no cookie
    get("/")
      .then()
        .statusCode(200)
      .header(Headers.SET_COOKIE, isEmptyOrNullString());

    // create session
    final String cookieHeader = get("/new")
      .then()
        .statusCode(200)
      .extract()
        .header(Headers.SET_COOKIE);

    assertNotNull(cookieHeader);

    // make a new request to / with cookie should return again the same cookie
    String cookieHeader2 = given()
      .header(Headers.COOKIE, cookieHeader)
    .when()
      .get("/")
    .then()
      .statusCode(200)
    .extract()
      .header(Headers.SET_COOKIE);

    // the session should be the same, so no set-cookie
    assertNull(cookieHeader2);

    // end the session
    String cookieHeader3 = given()
      .header(Headers.COOKIE, cookieHeader)
    .when()
      .get("/delete")
    .then()
      .statusCode(200)
    .extract()
      .header(Headers.SET_COOKIE);

    // there should be a set-cookie with maxAge 0
    assertNotNull(cookieHeader3);
    assertTrue(cookieHeader3.startsWith("yoke.sess=;"));
  }
}
