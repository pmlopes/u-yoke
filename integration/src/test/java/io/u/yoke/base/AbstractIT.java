package io.u.yoke.base;

import io.u.yoke.Yoke;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.ServiceLoader;

@RunWith(Parameterized.class)
public abstract class AbstractIT {

  @Parameters(name = "{0}")
  public static Iterable<Yoke> data() {
    return ServiceLoader.load(Yoke.class);
  }

  // here we can keep track of the last implementation
  // so we can speed up the setup/tear down
  private static Yoke last;

  @Parameter
  public Yoke app;


  @Before
  public void setUp() throws Exception {
    if (app != last) {
      // close the last
      if (last != null) {
        last.close();
      }

      app.listen(8080);
    }

    last = app;
  }

  @After
  public void tearDown() {
    // clear yoke
    app.clear();
  }
}
