/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.VertxContext;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

/**
 * # Timeout
 * <p>
 * Times out the getRequest in ```ms```, defaulting to ```5000```.
 * <p>
 * The timeout error is passed to ```next.handle(408)``` so that you may customize the getResponse behaviour.
 */
public class Timeout implements Handler<Context> {

  private final long timeout;

  public Timeout(final long timeout) {
    this.timeout = timeout;
  }

  public Timeout() {
    this(5000);
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    final VertxContext _ctx = (VertxContext) ctx;
    final long timerId = _ctx.getVertx().setTimer(timeout, event -> ctx.fail(Status.REQUEST_TIMEOUT));

    ctx.getResponse().endHandler(event -> _ctx.getVertx().cancelTimer(timerId));
    ctx.next();
  }
}
