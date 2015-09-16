package io.u.yoke;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ErrorHandler<T> {
    void handle(@NotNull T ctx, @NotNull YokeException exception);
}
