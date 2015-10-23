package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Yoke;
import io.u.yoke.http.header.Headers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static io.u.yoke.test.Yoke.yoke;
import static org.junit.Assert.fail;

public class VHostTest {

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
