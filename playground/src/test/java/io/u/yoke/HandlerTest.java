package io.u.yoke;

import org.junit.Test;

import static org.junit.Assert.*;

public class HandlerTest {

  @Test
  public void test1() {
    Handler<Object> handler = (obj) -> {
      next();
    };

    assertNotNull(handler);

    handler.handle(null);
  }
}