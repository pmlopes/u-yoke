package io.u.yoke.handler;

import io.u.yoke.base.AbstractIT;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RouterIT extends AbstractIT {

  @Test
  public void testRouterWithParams() {
    app.use(new Router()
        .get("/api/:userId", ctx -> {
          assertNotNull(ctx.getAt("user"));
          assertTrue(ctx.getAt("user") instanceof Map);
          ctx.end("OK");
        })
        .param("userId", ctx -> {
          assertEquals("1", ctx.getRequest().getParam("userId"));
          // pretend that we went on some DB and got a json object representing the user
          ctx.putAt("user", new LinkedHashMap<String, Object>() {{ put("id", ctx.getRequest().getParam("userId")); }});
          ctx.next();
        }));

    get("/api/1").then()
        .statusCode(200)
        .body(equalTo("OK"));
  }

  @Test
  public void testRouterWithRegExParamsFail() {
    app.use(new Router()
        .get("/api/:userId", ctx -> {
          ctx.end("OK");
        })
        .param("userId", Pattern.compile("[1-9][0-9]"))
    );

    get("/api/1").then()
        .statusCode(400);
  }

  @Test
  public void testRouterWithRegExParamsPass() {
    app.use(new Router()
            .get("/api/:userId", ctx -> {
              ctx.end("OK");
            })
            .param("userId", Pattern.compile("[1-9][0-9]"))
    );

    get("/api/10").then()
        .statusCode(200)
    .body(equalTo("OK"));
  }

  @Test
  public void testTrailingSlashes() {
    app.use(new Router()
        .get("/api", ctx -> {
          ctx.end("OK");
        }));

    get("/api").then()
        .statusCode(200)
        .body(equalTo("OK"));

    get("/api/").then()
        .statusCode(200)
        .body(equalTo("OK"));
  }

  @Test
  public void testDash() {
    app.use(new Router()
        .get("/api-stable", ctx -> {
          ctx.end("OK");
        }));

    get("/api-stable").then()
        .statusCode(200)
        .body(equalTo("OK"));
  }
}
