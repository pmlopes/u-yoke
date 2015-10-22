/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Response;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;

/**
 * # Session
 */
public class Session implements Handler<Context> {

  private final String name;
  private final String path;
  private final Boolean httpOnly;
  private final Boolean secure;
  private final long maxAge;

  public Session(@NotNull final String name, @NotNull final String path, final boolean secure, final boolean httpOnly, final long maxAge) {
    this.name = name;
    this.path = path;
    this.secure = secure;
    this.httpOnly = httpOnly;
    this.maxAge = maxAge;
  }

  public Session(@NotNull final String path, final boolean secure, final boolean httpOnly, final long maxAge) {
    this("yoke.sess", path, secure, httpOnly, maxAge);
  }

  public Session(@NotNull final String path, final boolean httpOnly, final long maxAge) {
    this("yoke.sess", path, true, httpOnly, maxAge);
  }

  public Session(final boolean httpOnly, final long maxAge) {
    this("yoke.sess", "/", true, httpOnly, maxAge);
  }

  public Session(final long maxAge) {
    this("yoke.sess", "/", true, true, maxAge);
  }

  public Session() {
    this("yoke.sess", "/", true, true, 30 * 60);
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    // path validation mismatch
    // TODO: verify if it is the normalized path
    if (ctx.getPath().indexOf(path) != 0) {
      ctx.next();
      return;
    }

    // default session
    final HttpCookie cookie = new HttpCookie(name, "");
    cookie.setPath(path);
    cookie.setHttpOnly(httpOnly);
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secure);

    // find the session cookie
    final HttpCookie sessionCookie = ctx.getCookie(name);
    final Response response = ctx.getResponse();

    final String sessionId = sessionCookie != null ? sessionCookie.getValue() : null;

    // call us when headers are being putAt for the getResponse
    response.headersHandler(done -> {
      String currentSessionId = ctx.getSessionId();

      // session was removed
      if (currentSessionId == null) {
        if (sessionCookie != null) {
          cookie.setValue("");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      } else {
        // only send secure cookies over https
        if (cookie.getSecure() && !ctx.isSecure()) {
          // TODO: report warning
          System.out.println("WARN: sending cookies not using HTTP can be hazardous");
        }

        // compare hashes, no need to set-cookie if unchanged
        if (!currentSessionId.equals(sessionId)) {
          // update value since the 2 have changed
          cookie.setValue(currentSessionId);

          response.addCookie(cookie);
        }
      }
    });

    if (sessionId == null) {
      ctx.next();
      return;
    }

    ctx.loadSession(sessionId, res -> ctx.next());
  }
}
