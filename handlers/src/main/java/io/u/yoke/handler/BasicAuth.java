/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * # BasicAuth
 * <p/>
 * Enforce basic authentication by providing a AuthCallback.engine(user, pass), which must return true in order to gain
 * access. Populates request.user. The final alternative is simply passing username / password strings.
 */
public class BasicAuth implements Handler<Context> {

  /**
   * Realm name for the application
   */
  private final String realm;

  /**
   * AuthCallback for validating this instance authentication requests.
   */
  private final AuthCallback authHandler;

  /**
   * Creates a new BasicAuth engine with a master username / password and a given realm.
   * <pre>
   *   Yoke yoke = new Yoke(...);
   *     yoke.use("/admin", new BasicAuth("admin", "s3cr37",
   *         "MyApp Auth Required"));
   * </pre>
   *
   * @param username the security principal user name
   * @param password the security principal password
   * @param realm    the security realm
   */
  public BasicAuth(@NotNull final String username, @NotNull final String password, @NotNull String realm) {
    this.realm = realm;
    authHandler = (_username, _password, cb) -> {
      boolean success = username.equals(_username) && password.equals(_password);
      if (success) {
        cb.call(_username);
      } else {
        cb.call("Bad credentials", null);
      }
    };
  }

  /**
   * Creates a new BasicAuth engine with a master username / password. By default the realm will be `Authentication required`.
   * <p/>
   * <pre>
   *       Yoke yoke = new Yoke(...);
   *       yoke.use("/admin", new BasicAuth("admin", "s3cr37"));
   * </pre>
   *
   * @param username the security principal user name
   * @param password the security principal password
   */
  public BasicAuth(@NotNull String username, @NotNull String password) {
    this(username, password, "Authentication required");

  }

  /**
   * Creates a new BasicAuth engine with a AuthCallback and a given realm.
   * <p/>
   * <pre>
   *       Yoke yoke = new Yoke(...);
   *       yoke.use("/admin", new AuthCallback() {
   *         public void handle(String user, String password, JMXHandler next) {
   *           // a better example would be fetching user from a DB
   *           if ("user".equals(user) &amp;&amp; "pass".equals(password)) {
   *             next.handle(true);
   *           } else {
   *             next.handle(false);
   *           }
   *         }
   *       }, "My App Auth");
   * </pre>
   *
   * @param authHandler the authentication engine
   * @param realm       the security realm
   */
  public BasicAuth(@NotNull String realm, @NotNull AuthCallback authHandler) {
    this.realm = realm;
    this.authHandler = authHandler;
  }

  /**
   * Creates a new BasicAuth engine with a AuthCallback.
   * <p/>
   * <pre>
   *       Yoke yoke = new Yoke(...);
   *       yoke.use("/admin", new AuthCallback() {
   *         public void handle(String user, String password, JMXHandler next) {
   *           // a better example would be fetching user from a DB
   *           if ("user".equals(user) &amp;&amp; "pass".equals(password)) {
   *             next.handle(true);
   *           } else {
   *             next.handle(false);
   *           }
   *         }
   *       });
   * </pre>
   *
   * @param authHandler the authentication engine
   */
  public BasicAuth(@NotNull AuthCallback authHandler) {
    this("Authentication required", authHandler);
  }

  /**
   * Handle all forbidden errors, in this case we need to add a special header to the response
   *
   * @param ctx yoke context
   */
  private void handle401(final Context ctx) {
    ctx.set("WWW-Authenticate", "Basic realm=\"" + getRealm(ctx) + "\"");
    ctx.fail(Status.UNAUTHORIZED, "No authorization token");
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    String authorization = ctx.get("Authorization");

    if (authorization == null) {
      handle401(ctx);
    } else {
      final String user;
      final String pass;
      final String scheme;

      try {
        String[] parts = authorization.split(" ");
        scheme = parts[0];
        String[] credentials = new String(Base64.getUrlDecoder().decode(parts[1]), Charset.forName("UTF-8")).split(":");
        user = credentials[0];
        // when the header is: "user:"
        pass = credentials.length > 1 ? credentials[1] : null;
      } catch (ArrayIndexOutOfBoundsException e) {
        handle401(ctx);
        return;
      } catch (IllegalArgumentException | NullPointerException e) {
        // IllegalArgumentException includes PatternSyntaxException
        ctx.fail(e);
        return;
      }

      if (!"Basic".equals(scheme)) {
        ctx.fail(Status.BAD_REQUEST);
      } else {
        authHandler.handle(user, pass, (err, res) -> {
          if (err == null) {
            ctx.putAt("user", user);
            ctx.next();
          } else {
            handle401(ctx);
          }
        });
      }
    }
  }

  /**
   * Get the realm for this instance
   * <p/>
   * The usecase is a multitenant app where I want different realms for paths like /foo/homepage and /bar/homepage.
   *
   * @param ctx http context request
   * @return realm name
   */
  public String getRealm(@NotNull Context ctx) {
    return realm;
  }
}
