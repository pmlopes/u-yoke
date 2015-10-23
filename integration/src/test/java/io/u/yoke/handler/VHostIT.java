package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.base.AbstractIT;
import io.u.yoke.http.header.Headers;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.fail;

public class VHostIT extends AbstractIT {

  @Test
  public void testVHost() {
    app.use(new Vhost("*.com", Context::end));

    app.use(ctx -> {
      fail();
    });

    given()
        .header(Headers.HOST, "www.mycorp.com")
    .when()
        .get("/upload").then()
        .statusCode(200);
  }
}
