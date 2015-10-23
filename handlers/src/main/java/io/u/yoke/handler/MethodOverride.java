/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Method;
import io.u.yoke.http.Request;
import io.u.yoke.http.form.Form;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.u.yoke.http.Method.*;

/**
 * # MethodOverride
 * <p>
 * Pass an optional ```key``` to use when checking for a method override, othewise defaults to *_method* or the header
 * *x-http-method-override*. The original method is available via ```req.originalMethod```.
 * <p>
 * *note:* If the method override is sent in a *POST* then the [BodyParser](BodyParser.html) middleware must be used and
 * installed prior this one.
 */
public class MethodOverride implements Handler<Context> {

  private final String key;

  public MethodOverride(@NotNull final String key) {
    this.key = key;
  }

  public MethodOverride() {
    this("_method");
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    final Request req = ctx.getRequest();

    // other methods than GET, HEAD and OPTIONS may have body
    if (GET != req.getMethod() && HEAD != req.getMethod() && OPTIONS != req.getMethod()) {
      try {
        final Form form = req.getBody();

        if (form != null) {
          String method = form.getParam(key);
          if (method != null) {
            form.remove(key);
            req.setMethod(Method.valueOf(method));
            ctx.next();
            return;
          }
        }
      } catch (RuntimeException e) {
        // this was not a form but something else
      }

      try {
        final Map json = req.getJSONBody();

        if (json != null) {
          String method = (String) json.get(key);
          if (method != null) {
            json.remove(key);
            req.setMethod(Method.valueOf(method));
            ctx.next();
            return;
          }
        }
      } catch (RuntimeException e) {
        // this was not JSON but something else
      }
    }

    String xHttpMethodOverride = req.getHeader("x-http-setmethod-override");

    if (xHttpMethodOverride != null) {
      req.setMethod(Method.valueOf(xHttpMethodOverride));
    }

    // if reached the end continue to the next middleware
    ctx.next();
  }
}
