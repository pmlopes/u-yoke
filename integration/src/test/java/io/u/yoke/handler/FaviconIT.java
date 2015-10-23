package io.u.yoke.handler;

import io.u.yoke.base.AbstractIT;
import io.u.yoke.http.header.Headers;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class FaviconIT extends AbstractIT {

  @Test
  public void testFavicon() {
    app.use(new Favicon());

    // first time is forbidden
    get("/favicon.ico").then()
        .statusCode(200)
        .header(Headers.CONTENT_TYPE, equalTo("image/x-icon"))
        .header(Headers.CACHE_CONTROL, notNullValue())
        .header(Headers.CONTENT_LENGTH, equalTo("1150"))
        .body(notNullValue());
  }
}
