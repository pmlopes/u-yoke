/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.form.Form;
import io.u.yoke.http.Method;
import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractRequest;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import org.jetbrains.annotations.NotNull;

/**
 * # BodyParser
 * <p>
 * Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.
 * <p>
 * Once data has been parsed the result is visible in the field `body` of the request.
 * <p>
 * If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
 * `Map&lt;String, HttpServerFileUpload&gt;`.
 * <p>
 * ### Limitations
 * <p>
 * Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
 * is preserved.
 */
public class BodyParser implements Handler<Context> {

  /**
   * Location on the file system to store the uploaded files.
   */
  private final String uploadDir;

  /**
   * Instantiates a Body parser with a configurable upload directory.
   * <p>
   * <pre>
   *      Yoke yoke = new Yoke(...);
   *      yoke.use(new BodyParser("/upload"));
   * </pre>
   *
   * @param uploadDir upload directory path
   */
  public BodyParser(@NotNull String uploadDir) {
    this.uploadDir = uploadDir;
  }

  /**
   * Instantiates a Body parser using the system default temp directory.
   * <p>
   * <pre>
   *      Yoke yoke = new Yoke(...);
   *      yoke.use(new BodyParser());
   * </pre>
   */
  public BodyParser() {
    this(System.getProperty("java.io.tmpdir"));
  }

  /**
   * JMXHandler for the parser. When the request method is GET or HEAD this is a Noop engine.
   * If not the engine verifies if there is a body and according to its header tries to
   * parse it as JSON, form data or multi part upload.
   *
   * @param ctx http yoke context
   */
  @Override
  public void handle(@NotNull final Context ctx) {
    final Method method = ctx.request().getMethod();

    // GET and HEAD have no setBody
    if (method == Method.GET || method == Method.HEAD || !ctx.request().hasBody()) {
      ctx.next();
    } else {

      final String contentType = ctx.get("Content-Type");

      final boolean isJSON = contentType != null && contentType.contains("application/json");
      final boolean isMULTIPART = contentType != null && contentType.contains("multipart/form-data");
      final boolean isURLENCODEC = contentType != null && contentType.contains("application/x-www-form-urlencoded");
      final Buffer buffer = (!isMULTIPART && !isURLENCODEC) ? Buffer.buffer() : null;

      // enable the parsing at Vert.x level
      HttpServerRequest nativeRequest = ctx.request().getNativeRequest();
      nativeRequest.setExpectMultipart(true);

      if (isMULTIPART) {
        nativeRequest.uploadHandler(fileUpload -> {
//          if (request.files() == null) {
//            request.setFiles(new HashMap<String, YokeFileUpload>());
//          }
//          final YokeFileUpload upload = new YokeFileUpload(vertx(), fileUpload, uploadDir);

          // setup callbacks
          fileUpload.exceptionHandler(ctx::fail);

//          // stream to the generated path
//          fileUpload.streamToFileSystem(upload.path());
//          // store a reference in the request
//          request.files().put(fileUpload.name(), upload);

          // putAt up a callback to remove the file from the file system when the request completes
          ctx.response().endHandler(v -> {
//            if (upload.isTransient()) {
//              upload.delete();
//            }
          });
        });
      }

      nativeRequest.handler(new io.vertx.core.Handler<Buffer>() {
        long size = 0;
        final long limit = ctx.request().getMaxLength();

        @Override
        public void handle(Buffer event) {
          if (limit != -1) {
            size += event.length();
            if (size < limit) {
              if (!isMULTIPART && !isURLENCODEC) {
                buffer.appendBuffer(event);
              }
            } else {
              nativeRequest.handler(null);
              nativeRequest.endHandler(null);

              ctx.putAt("canceled", true);
              ctx.fail(Status.PAYLOAD_TOO_LARGE);
            }
          } else {
            if (!isMULTIPART && !isURLENCODEC) {
              buffer.appendBuffer(event);
            }
          }
        }
      });

      nativeRequest.endHandler(v -> {
        if (isJSON) {
          if (buffer != null && buffer.length() > 0) {
            ((AbstractRequest) ctx.request()).setBody(buffer);
            if (!ctx.getAt("canceled", false)) {
              ctx.next();
            }
          } else if (buffer != null && buffer.length() == 0) {
            // special case for IE and Safari than even for 0 content length, send content type header
            if (ctx.request().getLength() == 0) {
              ((AbstractRequest) ctx.request()).setBody(null);

              if (!ctx.getAt("canceled", false)) {
                ctx.next();
              }
            } else {
              ctx.fail(Status.BAD_REQUEST);
            }
          } else {
            ctx.fail(Status.BAD_REQUEST);
          }
        } else {
          if (buffer != null) {
            ((AbstractRequest) ctx.request()).setBody(buffer);
          } else {
            final MultiMap form = nativeRequest.formAttributes();

            if (form != null) {
              ((AbstractRequest) ctx.request()).setBody(new Form() {
                @Override
                public String getParam(String parameter) {
                  return form.get(parameter);
                }

                @Override
                public Iterable<String> getParamValues(String parameter) {
                  return form.getAll(parameter);
                }

                @Override
                public Iterable<String> getParams() {
                  return form.names();
                }
              });
            }
          }
          if (!ctx.getAt("canceled", false)) {
            ctx.next();
          }
        }
      });
    }
  }
}
