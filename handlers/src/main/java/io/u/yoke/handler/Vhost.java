/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * # Vhost
 * <p>
 * Setup vhost for the given *hostname* and *server*.
 */
public class Vhost implements Handler<Context> {

  private final Handler<Context> handler;
  private final Pattern regex;

  /**
   * Create a new Vhost middleware. This middleware will verify the getRequest hostname and if it matches it will send
   * the getRequest to the registered handler, otherwise will continue inside the middleware chain.
   * <p>
   * <pre>
   * new Yoke(...)
   *   .use(new Vhost("*.jetdrone.com", existingHttpServerObject))
   * </pre>
   *
   * @param hostname
   * @param handler
   */
  public Vhost(@NotNull final String hostname, @NotNull final Handler<Context> handler) {
    this.handler = handler;
    this.regex = Pattern.compile("^" + hostname.replaceAll("\\.", "\\\\.").replaceAll("[*]", "(.*?)") + "$", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    String host = ctx.getRequest().getHeader("Host");
    if (host == null) {
      ctx.next();
    } else {
      boolean match = false;
      for (String h : host.split(":")) {
        if (regex.matcher(h).find()) {
          match = true;
          break;
        }
      }

      if (match) {
        handler.handle(ctx);
      } else {
        ctx.next();
      }
    }
  }
}
