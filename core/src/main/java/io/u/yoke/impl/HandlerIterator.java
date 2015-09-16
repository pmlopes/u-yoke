package io.u.yoke.impl;

import io.u.yoke.Context;
import io.u.yoke.ErrorHandler;
import io.u.yoke.Handler;
import io.u.yoke.YokeException;
import io.u.yoke.http.Status;

import java.util.List;

final class HandlerIterator implements Handler<YokeException> {

  private static final int START = 0;
  private static final int RUNNING = 1;
  private static final int AWAIT = 2;
  private static final int COMPLETE = 3;

  private final List<? extends Handler<Context>> handlers;
  private final ErrorHandler<Context> errHandler;
  private final Handler<Context> endHandler;

  private final Context ctx;

  private int idx = -1;
  private int state = START;

  HandlerIterator(List<? extends Handler<Context>> handlers, ErrorHandler<Context> errHandler, Context ctx) {
    this.handlers = handlers;
    this.errHandler = errHandler;
    this.endHandler = ctx1 -> errHandler.handle(ctx, new YokeException(Status.NOT_FOUND));
    this.ctx = ctx;
  }

  HandlerIterator(List<? extends Handler<Context>> handlers, HandlerIterator parent) {
    this.handlers = handlers;
    this.errHandler = parent.errHandler;
    this.endHandler = ctx -> {
      ((AbstractContext) ctx).setIterator(parent);
      ctx.next();
    };
    this.ctx = parent.ctx;
  }

  @Override
  public void handle(YokeException err) {
    if (err != null) {
      errHandler.handle(ctx, err);
      return;
    }

    if (state == RUNNING) {
      // this was a recursive call (blocking code)
      state = COMPLETE;
      return;
    }
    while (++idx < handlers.size()) {
      final Handler<Context> handler = handlers.get(idx);
      state = RUNNING;
      handler.handle(ctx);
      if (state == COMPLETE) {
        continue;
      }
      state = AWAIT;
      return;
    }

    // no more handlers
    endHandler.handle(ctx);
  }
}
