package io.u.yoke.traits;

import org.jetbrains.annotations.NotNull;

public interface StoreTrait {

  /**
   * Allow getting properties in a generified way.
   *
   * @param name The key to get
   * @return {R} The found object
   */
  <R> R getAt(@NotNull final String name);

  /**
   * Allow getting properties in a generified way and return defaultValue if the key does not exist.
   *
   * @param name         The key to get
   * @param defaultValue value returned when the key does not exist
   * @return {R} The found object
   */
  <R> R getAt(@NotNull final String name, R defaultValue);

  /**
   * Allows putting a value into the context
   *
   * @param name  the key to store
   * @param value the value to store
   * @return {R} the previous value or null
   */
  <R> R putAt(@NotNull final String name, R value);
}
