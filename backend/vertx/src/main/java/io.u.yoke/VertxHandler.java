package io.u.yoke;


import io.vertx.core.Vertx;

@FunctionalInterface
public interface VertxHandler<T> extends Handler<T> {
  default void setVertx(Vertx vertx) {
    // NO-OP
  }
}
