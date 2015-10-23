package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Request;
import io.u.yoke.http.Status;
import io.u.yoke.security.Security;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;

import static io.u.yoke.http.Method.*;
import static io.u.yoke.http.header.Headers.*;

public class JWT implements Handler<Context> {

  private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

  private io.u.yoke.security.JWT jwt;

  private final Handler<Context> handler;

  public JWT(@NotNull final Security security) {
    this(security, null);
  }

  public JWT(@NotNull final Security security, final Handler<Context> handler) {
    this.jwt = new io.u.yoke.security.JWT(security);
    this.handler = handler;
  }

  @Override
  public void handle(@NotNull final Context ctx) {

    final Request req = ctx.getRequest();

    String token = null;

    if (OPTIONS == req.getMethod() && ctx.get(ACCESS_CONTROL_REQUEST_HEADERS) != null) {
      for (String ctrlReq : ctx.get(ACCESS_CONTROL_REQUEST_HEADERS).split(",")) {
        if (ctrlReq.contains(AUTHORIZATION)) {
          ctx.next();
          return;
        }
      }
    }

    final String authorization = ctx.get(AUTHORIZATION);

    if (authorization != null) {
      String[] parts = authorization.split(" ");
      if (parts.length == 2) {
        final String scheme = parts[0],
            credentials = parts[1];

        if (BEARER.matcher(scheme).matches()) {
          token = credentials;
        }
      } else {
        // Format is Authorization: Bearer [token]
        ctx.fail(Status.UNAUTHORIZED);
        return;
      }
    } else {
      // No Authorization header was found
      ctx.fail(Status.UNAUTHORIZED);
      return;
    }

    try {
      final Map jwtToken = jwt.decode(token);

      // All dates in JWT are of type NumericDate
      // a NumericDate is: numeric value representing the number of seconds from 1970-01-01T00:00:00Z UTC until
      // the specified UTC date/time, ignoring leap seconds
      final long now = System.currentTimeMillis() / 1000;

      if (jwtToken.containsKey("iat")) {
        Long iat = (Long) jwtToken.get("iat");
        // issue at must be in the past
        if (iat > now) {
          ctx.fail(Status.UNAUTHORIZED);
          return;
        }
      }

      if (jwtToken.containsKey("nbf")) {
        Long nbf = (Long) jwtToken.get("nbf");
        // not before must be after now
        if (nbf > now) {
          ctx.fail(Status.UNAUTHORIZED);
          return;
        }
      }

      if (jwtToken.containsKey("exp")) {
        Long exp = (Long) jwtToken.get("exp");
        // expires must be after now
        if (now > exp) {
          ctx.fail(Status.UNAUTHORIZED);
          return;
        }
      }
      ctx.putAt("jwt", jwtToken);

      if (handler == null) {
        ctx.next();
        return;
      }

      handler.handle(ctx);
    } catch (RuntimeException e) {
      ctx.fail(e);
    }
  }
}
