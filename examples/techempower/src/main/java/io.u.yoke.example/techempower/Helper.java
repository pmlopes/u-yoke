package io.u.yoke.example.techempower;

import io.u.yoke.Context;

import java.util.concurrent.ThreadLocalRandom;

public final class Helper {

  private Helper() {
  }

  /**
   * Returns the value of the "queries" request parameter, which is an integer
   * bound between 1 and 500 with a default value of 1.
   *
   * @param ctx the current HTTP exchange
   * @return the value of the "queries" request parameter
   */
  static int getQueries(Context ctx) {
    String param = ctx.request().getParam("queries");

    if (param == null) {
      return 1;
    }
    try {
      int parsedValue = Integer.parseInt(param);
      return Math.min(500, Math.max(1, parsedValue));
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  /**
   * Returns a random integer that is a suitable value for both the {@code id}
   * and {@code randomNumber} properties of a world object.
   *
   * @return a random world number
   */
  static int randomWorld() {
    return 1 + ThreadLocalRandom.current().nextInt(10000);
  }
}
