package io.u.yoke.handler;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.u.yoke.Yoke;
import io.u.yoke.http.Status;
import io.u.yoke.http.form.Form;
import io.u.yoke.http.header.Headers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static io.u.yoke.test.Yoke.yoke;
import static org.junit.Assert.*;

public class BodyParserTest {

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
  public void testJsonBodyParser() {

    final String json = "{\"key\":\"value\"}";

    app.use(new BodyParser());
    app.use(ctx -> {

      assertNotNull(ctx.getRequest().getJSONBody());
      assertEquals("value", ctx.getRequest().getJSONBody().get("key"));
      ctx.end();
    });

    given()
        .header(Headers.CONTENT_TYPE, "application/json")
        .body(json)
    .when()
        .post("/upload").then()
        .statusCode(200);
  }

  @Test
  public void testMapBodyParser() {

    app.use(new BodyParser());
    app.use(ctx -> {
      Form form = ctx.getRequest().getBody();
      assertEquals("value", form.getParam("param"));
      ctx.end();
    });

    given()
        .header(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded")
        .body("param=value")
    .when()
        .post("/upload").then()
        .statusCode(200);
  }

  @Test
  public void testTextBodyParser() {

    app.use(new BodyParser());
    app.use(ctx -> {
      byte[] body = ctx.getRequest().getBody();
      assertEquals("hello-world", new String(body));
      ctx.end();
    });

    given()
        .body("hello-world")
    .when()
        .post("/upload").then()
        .statusCode(200);
  }

  @Test
  public void testBodyParserWithEmptyBody() {

    app.use(new BodyParser());
    app.use(ctx -> {
      ctx.end();
    });

    delete("/upload").then()
        .statusCode(200);
  }

  @Test
  public void testJsonBodyLengthLimit() {

    app.use(new Limit(5));
    app.use(new BodyParser());
    app.use(ctx -> {
        fail("Body should have been too long");
    });

    given()
        .header(Headers.CONTENT_TYPE, "application/json")
        .body("[1,2,3,4,5]")
    .when()
        .post("/upload").then()
        .statusCode(413);
  }

  @Test
  public void testTextBodyLengthLimit() {

    app.use(new Limit(5L));
    app.use(new BodyParser());
    app.use(ctx -> {
      fail("Body should have been too long");
    });

    given()
        .config(RestAssured.config().encoderConfig(encoderConfig().encodeContentTypeAs("plain/text", ContentType.TEXT)))
        .header(Headers.CONTENT_TYPE, "plain/text")
        .body("hello world")
    .when()
        .post("/upload").then()
        .statusCode(413);
  }

  @Test
  public void testFormEncodedBodyLengthLimit() {

    app.use(new Limit(5L));
    app.use(new BodyParser());
    app.use(ctx -> {
      fail("Body should have been too long");
    });

    given()
        .header(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded")
        .body("hello=world")
    .when()
        .post("/upload").then()
        .statusCode(413);
  }

  @Test
  public void testDeleteContentLengthZeroWithNoBody() {

    app.use(new BodyParser());
    app.use(ctx -> {
      ctx.setStatus(Status.NO_CONTENT);
      ctx.end();
    });

    given()
        .header(Headers.CONTENT_TYPE, "application/json")
        .body("")
    .when()
        .post("/upload").then()
        .statusCode(204);
  }

  @Test
  public void testMultipartFileUpload() throws IOException {

    app.use(new BodyParser());
    app.use(ctx -> {
      assertNotNull(ctx.getRequest().getFiles());
      File f = ctx.getRequest().getFile("file");
      assertTrue(f.exists());
      assertEquals(12l, f.length());
      ctx.end();
    });

    // create tmp file
    File tmp = File.createTempFile("yoke", "test");
    tmp.deleteOnExit();

    try (OutputStream out = new FileOutputStream(tmp)) {
      out.write("Hello World!".getBytes());
    }

    given()
        .multiPart(tmp)
    .when()
        .post("/fileUpload")
    .then()
        .statusCode(200);
  }
}
