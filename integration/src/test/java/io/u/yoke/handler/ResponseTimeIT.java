package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.base.AbstractIT;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.notNullValue;

public class ResponseTimeIT extends AbstractIT {

  @Test
  public void testResponseTime() {
    app.use(new ResponseTime());

    app.use(Context::end);

    get("/").then()
        .header("x-response-time", notNullValue())
        .statusCode(200);
  }
}
