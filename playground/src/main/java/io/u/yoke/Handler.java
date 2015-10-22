package io.u.yoke;

@FunctionalInterface
public interface Handler<T> {

  // This is the default functional interface
  void handle(T value);

  default void next() {

  }
}
