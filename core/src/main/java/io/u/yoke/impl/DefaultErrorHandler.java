package io.u.yoke.impl;

import io.u.yoke.Context;
import io.u.yoke.ErrorHandler;
import io.u.yoke.YokeException;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

public final class DefaultErrorHandler implements ErrorHandler<Context> {

  @Override
  public void handle(@NotNull Context ctx, @NotNull YokeException exception) {
    Status errorCode = exception.getStatus();
    if (errorCode.getCode() < 400) {
      errorCode = Status.INTERNAL_SERVER_ERROR;
    }

    ctx.response().setStatus(errorCode);
    ctx.response().setMessage(exception.getMessage());
    ctx.response().end(exception.getMessage());
  }
}
