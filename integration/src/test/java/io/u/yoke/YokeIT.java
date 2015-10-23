package io.u.yoke;

import io.u.yoke.base.AbstractIT;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class YokeIT extends AbstractIT {

  @Test
  public void testBootstrap() {
    get("/").then().statusCode(404);
  }

  @Test
  public void testUse() {
    app.use(Context::end);

    get("/").then().statusCode(200);
  }

  @Test
  public void testUseWithMount() {
    app.use("/hello", Context::end);

    get("/").then().statusCode(404);
    get("/hello").then().statusCode(200);
  }

  @Test
  public void testLocals() {
    app.putAt("user", "me");

    app.use(ctx -> {
      ctx.end(ctx.getAt("user"));
    });

    get("/").then()
        .statusCode(200)
        .body(equalTo("me"));
  }

  @Test
  public void testLocalsOverride() {
    app.putAt("user", "me");

    app.use(ctx -> {
      ctx.putAt("user", "you");
      ctx.end(ctx.getAt("user"));
    });

    get("/").then()
        .statusCode(200)
        .body(equalTo("you"));
  }

  @Test
  public void testLocalsDelete() {
    app.putAt("user", "me");

    app.use(ctx -> {
      ctx.putAt("user", "you");
      ctx.putAt("user", null);
      ctx.end(ctx.getAt("user"));
    });

    get("/").then()
        .statusCode(200)
        .body(equalTo("me"));
  }

  @Test
  public void testErrorHandler() {
    app.setErrorHandler((ctx, exception) -> {
      ctx.getResponse()
          .setStatus(exception.getStatus());
      ctx.end("oops!");
    });

    get("/").then()
        .statusCode(404)
        .body(equalTo("oops!"));
  }

}
