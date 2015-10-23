/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import java.util.regex.Pattern;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Request;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

import static io.u.yoke.http.Method.*;
import static io.u.yoke.http.header.Headers.*;

/**
 * Basic CORS support.
 */
public class Cors implements Handler<Context> {

  private final Pattern allowedOriginPattern;
  private final String allowedMethods;
  private final String allowedHeaders;
  private final String exposedHeaders;
  private final boolean allowCredentials;

  /**
   * @param allowedOriginPattern if null, '*' will be used.
   */
  public Cors(final Pattern allowedOriginPattern,
              final Iterable<String> allowedMethods,
              final Iterable<String> allowedHeaders,
              final Iterable<String> exposedHeaders,
              final boolean allowCredentials) {

    if (allowCredentials && allowedOriginPattern == null) {
      throw new IllegalArgumentException("Resource that supports credentials can't accept all origins.");
    }

    this.allowedOriginPattern = allowedOriginPattern;

    this.allowedMethods = join(allowedMethods, ",");
    this.allowedHeaders = join(allowedHeaders, ",");
    this.exposedHeaders = join(exposedHeaders, ",");

    this.allowCredentials = allowCredentials;
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    if (isPreflightRequest(ctx)) {
      handlePreflightRequest(ctx);
    } else {
      addCorsResponseHeaders(ctx);
      ctx.next();
    }
  }

  private boolean isPreflightRequest(final Context ctx) {
    final Request req = ctx.getRequest();

    return OPTIONS == req.getMethod() && (req.getHeader(ACCESS_CONTROL_REQUEST_HEADERS) != null || req.getHeader(ACCESS_CONTROL_REQUEST_METHOD) != null);
  }

  private void handlePreflightRequest(@NotNull final Context ctx) {
    if (isValidOrigin(ctx.get(ORIGIN))) {
      // set default status
      ctx.setStatus(Status.NO_CONTENT);
      addCorsResponseHeaders(ctx.get(ORIGIN), ctx);
      ctx.end();
    } else {
      ctx.setStatus(Status.FORBIDDEN);
      ctx.end();
    }
  }

  private void addCorsResponseHeaders(@NotNull final Context ctx) {
    final String origin = ctx.get(ORIGIN);
    addCorsResponseHeaders(origin, ctx);
  }

  private void addCorsResponseHeaders(final String origin, @NotNull final Context ctx) {
    if (isValidOrigin(origin)) {
      ctx.set(ACCESS_CONTROL_ALLOW_ORIGIN, getAllowedOrigin(origin));

      if (allowedMethods != null) {
        ctx.set(ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
      }

      if (allowedHeaders != null) {
        ctx.set(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
      }

      if (exposedHeaders != null) {
        ctx.set(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
      }

      if (allowCredentials) {
        ctx.set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      }
    }
  }

  private boolean isValidOrigin(final String origin) {
    return allowedOriginPattern == null || (isNotBlank(origin) && allowedOriginPattern.matcher(origin).matches());
  }

  private String getAllowedOrigin(final String origin) {
    return allowedOriginPattern == null ? "*" : origin;
  }

  private static boolean isNotBlank(final String s) {
    return s != null && !s.trim().isEmpty();
  }

  private static String join(final Iterable<String> ss, final String j) {
    if (ss == null) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final String s : ss) {
      if (!first) {
        sb.append(j);
      }
      sb.append(s);
      first = false;
    }

    if (sb.length() == 0) {
      return null;
    }

    return sb.toString();
  }
}
