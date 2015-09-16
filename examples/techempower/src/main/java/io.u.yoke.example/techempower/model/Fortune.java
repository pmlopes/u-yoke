package io.u.yoke.example.techempower.model;

import org.jetbrains.annotations.NotNull;

/**
 * The model for the "fortune" database table.
 */
public final class Fortune implements Comparable<Fortune> {

  private final int id;
  private final String message;

  /**
   * Constructs a new fortune object with the given parameters.
   *
   * @param id the ID of the fortune
   * @param message the message of the fortune
   */
  public Fortune(int id, String message) {
    this.id = id;
    this.message = message;
  }

  public int getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int compareTo(@NotNull Fortune other) {
    return message.compareTo(other.message);
  }
}