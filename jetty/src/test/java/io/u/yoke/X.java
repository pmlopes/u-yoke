package io.u.yoke;

import java.io.IOException;

import static io.u.yoke.test.Yoke.yoke;

public class X {

  public static void main(String[] args) throws IOException{
    Yoke yoke = yoke();
    yoke.use(ctx -> {
      System.out.println(ctx.request());
      ctx.end("Hello!");
    });
  }
}
