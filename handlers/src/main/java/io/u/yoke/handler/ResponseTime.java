/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Response;
import org.jetbrains.annotations.NotNull;

/**
 * # ResponseTime
 * <p>
 * Adds the ```x-getResponse-time``` header displaying the getResponse duration in milliseconds.
 */
public class ResponseTime implements Handler<Context> {
  @Override
  public void handle(@NotNull final Context ctx) {

    final long start = System.currentTimeMillis();
    final Response response = ctx.getResponse();

    ctx.getResponse().headersHandler(v -> {
        long duration = System.currentTimeMillis() - start;
        response.setHeader("X-Response-Time", duration + "ms");
    });

    ctx.next();
  }
}
