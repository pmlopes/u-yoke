package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Yoke;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static io.u.yoke.test.Yoke.yoke;
import static org.hamcrest.Matchers.notNullValue;

public class ResponseTimeTest {

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
  public void testResponseTime() {
    app.use(new ResponseTime());

    app.use(Context::end);

    get("/").then()
        .header("x-response-time", notNullValue())
        .statusCode(200);
  }
}
