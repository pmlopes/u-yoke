/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

/**
 * # Limit
 *
 * Limits the getRequest body to a specific amount of bytes. If the getRequest body contains more bytes than the allowed
 * limit an *413* error is sent back to the client.
 */
public class Limit implements Handler<Context> {

  /**
   * The max allowed length for the resource
   */
  private final long limit;

  /**
   * Creates a Limit instance with a given max allowed bytes
   *
   * <pre>
   * new Yoke(...)
   *   .use(new Limit(1024));
   * </pre>
   *
   * @param limit
   */
  public Limit(final long limit) {
    this.limit = limit;
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    if (ctx.getRequest().hasBody()) {
      ctx.getRequest().setMaxLength(limit);

      long len = ctx.getRequest().getLength();
      // limit by content-length
      if (len > limit) {
        ctx.fail(Status.PAYLOAD_TOO_LARGE);
        return;
      }
    }

    ctx.next();
  }
}
