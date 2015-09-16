package io.u.yoke;

@FunctionalInterface
public interface Callback<T> {

  default void call(String message, T result) {
    call(new YokeException(message), result);
  }

  default void call(T result) {
    call((Throwable) null, result);
  }

  void call(Throwable err, T result);
}