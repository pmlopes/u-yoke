package io.u.yoke.example.restful.routes;

import io.u.yoke.Context;
import io.u.yoke.Handler;

public class Index implements Handler<Context> {
  @Override
  public void handle(Context ctx) {
    ctx.putAt("title", "Yoke 3");

    ctx.getResponse().render("index");
  }
}
