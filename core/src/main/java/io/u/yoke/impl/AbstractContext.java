package io.u.yoke.impl;

import io.u.yoke.Context;
import io.u.yoke.ErrorHandler;
import io.u.yoke.Handler;
import io.u.yoke.YokeException;
import io.u.yoke.http.Status;
import io.u.yoke.util.OverlayMap;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractContext implements Context {

  private final Map<String, Object> appLocals;

  private OverlayMap locals;
  private HandlerIterator iterator;

  public AbstractContext(@NotNull final Map<String, Object> yokeLocals) {
    appLocals = yokeLocals;
  }


  public void setIterator(List<? extends Handler<Context>> handlers) {
    iterator = new HandlerIterator(handlers, iterator);
  }

  public void setIterator(List<? extends Handler<Context>> handlers, ErrorHandler<Context> errHandler) {
    iterator = new HandlerIterator(handlers, errHandler, this);
  }

  void setIterator(HandlerIterator iterator) {
    this.iterator = iterator;
  }

  public Map<String, Object> getLocals() {
    if (locals == null) {
      locals = new OverlayMap(appLocals);
    }

    return locals;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R getAt(@NotNull String key) {
    if (locals == null) {
      return (R) appLocals.get(key);
    }

    return (R) locals.get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R getAt(@NotNull String key, R defaultValue) {
    if (locals == null) {
      if (appLocals.containsKey(key)) {
        return (R) appLocals.get(key);
      } else {
        return defaultValue;
      }
    }

    if (locals.containsKey(key)) {
      return (R) locals.get(key);
    } else {
      return defaultValue;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> R putAt(@NotNull String key, R value) {
    // null values have a special semantic, they mean delete
    if (value != null && locals == null) {
      locals = new OverlayMap(appLocals);
    }

    if (locals != null) {
      if (value != null) {
        return (R) locals.put(key, value);
      } else {
        return (R) locals.remove(key);
      }
    }
    
    return null;
  }

  @Override
  public void next() {
    iterator.handle(null);
  }

  @Override
  public void fail(Status status, String message) {
    iterator.handle(new YokeException(status, message));
  }

  @Override
  public void fail(Status status, String message, Throwable cause) {
    iterator.handle(new YokeException(status, message, cause));
  }
}
