package io.u.yoke.handler;

import io.u.yoke.Yoke;
import io.u.yoke.http.header.Headers;
import org.junit.*;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static io.u.yoke.test.Yoke.*;

public class FaviconTest {

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
