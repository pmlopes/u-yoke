package io.u.yoke.test;

public class Yoke {

  private static io.u.yoke.Yoke instance;

  public synchronized static io.u.yoke.Yoke yoke() {
    if (instance == null) {
      // bootstrap yoke
      instance = io.u.yoke.Yoke.getDefault();
      instance.listen(8080);
    } else {
      instance.clear();
    }

    return instance;
  }
}
