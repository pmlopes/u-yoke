package io.u.yoke;

@FunctionalInterface
public interface Handler<T> {
  void handle(T value);
}
