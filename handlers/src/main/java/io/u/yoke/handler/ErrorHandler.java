/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.YokeException;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * # ErrorHandler
 * <p>
 * Creates pretty print error pages in *html*, *text* or *json* depending on the *accept* header from the client.
 */
public class ErrorHandler implements io.u.yoke.ErrorHandler<Context> {

  /**
   * Flag to enable/disable printing the full stack trace of exceptions.
   */
  private final boolean fullStack;

  /**
   * Cached template for rendering the html errors
   */
  private final String errorTemplate;

  /**
   * Loads a file from a input stream containing all known mime types. The InputStream is a resource mapped from the
   * project resource directory.
   */
  private static String loadFile(@NotNull String filename) {

    try (InputStream in = ErrorHandler.class.getClassLoader().getResourceAsStream(filename)) {
      final StringBuilder buffer = new StringBuilder();

      int read;
      byte[] data = new byte[4096];
      while ((read = in.read(data, 0, data.length)) != -1) {
        if (read == data.length) {
          buffer.append(new String(data, Charset.defaultCharset()));
        } else {
          buffer.append(new String(data, 0, read, Charset.defaultCharset()));
        }
      }

      return buffer.toString();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Create a new ErrorHandler allowing to print or not the stack trace. Include stack trace `true` might be useful in
   * development mode but probably you don't want it in production.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new ErrorHandler(true);
   * </pre>
   *
   * @param fullStack include full stack trace in error report.
   */
  public ErrorHandler(boolean fullStack) {
    this.fullStack = fullStack;
    errorTemplate = loadFile("io/u/yoke/error.html");
  }

  private boolean sendError(final Context ctx, final String mime, final YokeException exception) {

    Status errorCode;

    switch (mime) {
      case "text/html":
        errorCode = exception.getStatus();

        if (errorCode.getCode() < 400) {
          errorCode = Status.INTERNAL_SERVER_ERROR;
        }

        StringBuilder stackHtml = new StringBuilder();
        if (fullStack) {
          for (StackTraceElement e : exception.getStackTrace()) {
            stackHtml.append("<li>");
            stackHtml.append(e.toString());
            stackHtml.append("</li>");
          }
        }

        ctx.set("Content-Type", "text/html");
        ctx.response().end(errorTemplate.replace("{title}", ctx.getAt("title"))
                .replace("{errorCode}", Integer.toString(errorCode.getCode()))
                .replace("{errorMessage}", exception.getMessage())
                .replace("{stackTrace}", stackHtml.toString()));
        return true;

      case "application/json":
        errorCode = exception.getStatus();

        if (errorCode.getCode() < 400) {
          errorCode = Status.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("code", errorCode.getCode());
        json.put("message", exception.getMessage());
        if (fullStack) {
          List<String> stackJson = new LinkedList<>();
          for (StackTraceElement e : exception.getStackTrace()) {
            stackJson.add(e.toString());
          }
          json.put("stack", stackJson);
        }

        ctx.set("Content-Type", "application/json; UTF-8");
        ctx.response().json(json);
        return true;

      case "text/plain":
        errorCode = exception.getStatus();

        if (errorCode.getCode() < 400) {
          errorCode = Status.INTERNAL_SERVER_ERROR;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Error ");
        sb.append(errorCode.getCode());
        sb.append(": ");
        sb.append(exception.getMessage());

        if (fullStack) {
          for (StackTraceElement e : exception.getStackTrace()) {
            sb.append("\tat ");
            sb.append(e.toString());
            sb.append("\n");
          }
        }

        ctx.set("Content-Type", "text/plain; UTF-8");
        ctx.response().end(sb.toString());
        return true;

      default:
        return false;
    }
  }

  @Override
  public void handle(@NotNull final Context ctx, @NotNull final YokeException exception) {

    Status errorCode = exception.getStatus();
    if (errorCode.getCode() < 400) {
      errorCode = Status.INTERNAL_SERVER_ERROR;
    }

    // does the response already putAt the mime type?
    String mime = ctx.response().getHeader("content-type");

    if (mime != null) {
      if (sendError(ctx, mime, exception)) {
        return;
      }
    }

    // respect the client accept order
    for (String accept : ctx.request().getSortedHeader("Accept")) {
      if (sendError(ctx, accept, exception)) {
        return;
      }
    }

    // fall back plain/text
    sendError(ctx, "text/plain", exception);
  }
}
