package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.base.AbstractIT;
import io.u.yoke.http.header.Headers;
import io.u.yoke.security.Security;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.crypto.Mac;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SessionIT extends AbstractIT {

  private static final Mac hmac = Security.create("keyboard cat").getMac("HmacSHA256");
  private static String cookieHeader;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    app.use(new CookieParser(hmac));
    app.use(new Session("yoke.sess", "/", false, true, 30 * 60));

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
  }

  @Test
  public void step1_there_is_no_cookie() {
    // start: there is no cookie
    get("/")
      .then()
        .statusCode(200)
        .header(Headers.SET_COOKIE, isEmptyOrNullString());
  }

  @Test
  public void step2_create_session() {
    // create session
    cookieHeader = get("/new")
        .then()
        .statusCode(200)
        .extract()
        .header(Headers.SET_COOKIE);

    assertNotNull(cookieHeader);
  }

  @Test
  public void step3_should_return_no_cookie() {
    // make a new request to / with cookie should return again the same cookie
    String cookieHeader = given()
        .header(Headers.COOKIE, SessionIT.cookieHeader)
        .when()
        .get("/")
        .then()
        .statusCode(200)
        .extract()
        .header(Headers.SET_COOKIE);

    // the session should be the same, so no set-cookie
    assertNull(cookieHeader);
  }

  @Test
  public void step4_end_session() {
    // end the session
    cookieHeader = given()
        .header(Headers.COOKIE, cookieHeader)
        .when()
        .get("/delete")
        .then()
        .statusCode(200)
        .extract()
        .header(Headers.SET_COOKIE);

    // there should be a set-cookie with maxAge 0
    assertNotNull(cookieHeader);
    assertTrue(cookieHeader.startsWith("yoke.sess=;"));
  }
}
