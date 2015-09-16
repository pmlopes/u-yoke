/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Method;
import io.u.yoke.http.Status;
import io.u.yoke.impl.AbstractContext;
import jdk.nashorn.internal.objects.NativeRegExp;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * # Router
 * <p>
 * Route request by path or regular expression. All *HTTP* verbs are available:
 * <p>
 * * `GET`
 * * `PUT`
 * * `POST`
 * * `DELETE`
 * * `OPTIONS`
 * * `HEAD`
 * * `TRACE`
 * * `CONNECT`
 * * `PATCH`
 * <p>
 * Create a new Router Middleware.
 * <p>
 * <pre>
 * new Router() {{
 *   get("/hello", new JMXHandler&lt;YokeRequest&gt;() {
 *     public void handle(YokeRequest request) {
 *       request.response().end("Hello World!");
 *     }
 *   });
 * }}
 * </pre>
 */
public class Router implements Handler<Context> {

  private final Map<Method, List<Route>> routes = new EnumMap<>(Method.class);

  private final Map<String, Handler<Context>> paramHandlers = new HashMap<>();

  public Router() {
    for (Method m : Method.values()) {
      routes.put(m, new ArrayList<>());
    }
  }

  @Override
  public void handle(@NotNull final Context context) {

    final List<Route> handlers = routes.get(context.request().getMethod());
    final AbstractContext ctx = (AbstractContext) context;

    ctx.setIterator(handlers);
    ctx.next();
  }

  /**
   * Specify a handlers that will be called for a matching HTTP GET
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router get(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.GET, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PUT
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router put(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.PUT, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP POST
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router post(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.POST, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP DELETE
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router delete(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.DELETE, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP OPTIONS
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router options(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.OPTIONS, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP HEAD
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router head(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.HEAD, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP TRACE
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router trace(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.TRACE, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP CONNECT
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router connect(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.CONNECT, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PATCH
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router patch(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addPattern(Method.PATCH, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for all HTTP methods
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router all(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    get(pattern, handler);
    put(pattern, handler);
    post(pattern, handler);
    delete(pattern, handler);
    options(pattern, handler);
    head(pattern, handler);
    trace(pattern, handler);
    connect(pattern, handler);
    patch(pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP GET
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router get(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.GET, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PUT
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router put(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.PUT, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP POST
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router post(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.POST, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP DELETE
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router delete(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.DELETE, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP OPTIONS
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router options(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.OPTIONS, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP HEAD
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router head(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.HEAD, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP TRACE
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router trace(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.TRACE, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP CONNECT
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router connect(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.CONNECT, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PATCH
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router patch(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    addRegEx(Method.PATCH, regex, handlers);
    return this;
  }

  /**
   * Specify a handlers that will be called for all HTTP methods
   *
   * @param regex    A regular expression
   * @param handlers The handlers to call
   */
  public Router all(@NotNull final Pattern regex, @NotNull final Handler<Context> handlers) {
    get(regex, handlers);
    put(regex, handlers);
    post(regex, handlers);
    delete(regex, handlers);
    options(regex, handlers);
    head(regex, handlers);
    trace(regex, handlers);
    connect(regex, handlers);
    patch(regex, handlers);
    return this;
  }

  public Router param(@NotNull final String paramName, @NotNull final Handler<Context> handler) {
    paramHandlers.put(paramName, handler);
    for (List<Route> routes : this.routes.values()) {
      for (Route route : routes) {
        route.addParam(paramName, handler);
      }
    }
    return this;
  }

  public Router param(@NotNull final String paramName, @NotNull final NativeRegExp regex) {
    return param(paramName, (@NotNull final Context ctx) -> {
      if (!regex.test(ctx.request().getParam(paramName))) {
        // Bad Request
        ctx.fail(Status.BAD_REQUEST);
        return;
      }

      ctx.next();
    });
  }

  public Router param(@NotNull final String paramName, @NotNull final Pattern regex) {
    return param(paramName, (@NotNull final Context ctx) -> {
      if (!regex.matcher(ctx.request().getParam(paramName)).matches()) {
        // Bad Request
        ctx.fail(Status.BAD_REQUEST);
        return;
      }

      ctx.next();
    });
  }

  private void addPattern(Method verb, String input, Handler<Context> handler) {
    // We need to search for any :<token name> tokens in the String and replace them with named capture groups
    Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(input);
    StringBuffer sb = new StringBuffer();
    Set<String> groups = new HashSet<>();
    while (m.find()) {
      String group = m.group().substring(1);
      if (groups.contains(group)) {
        throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
      }
      m.appendReplacement(sb, "(?<$1>[^\\/]+)");
      groups.add(group);
    }
    m.appendTail(sb);
    // ignore tailing slash if not part of the input, not really REST but common on other frameworks
    if (sb.charAt(sb.length() - 1) != '/') {
      sb.append("\\/?$");
    }

    // verify if the binding already exists, if yes add to it
    for (Route route : routes.get(verb)) {
      if (route.isFor(input)) {
        route.addHandler(verb.name(), handler);
        return;
      }
    }

    final Route route = new Route(input, Pattern.compile(sb.toString()), groups);

    route.addHandler(verb.name(), handler);

    for (String param : groups) {
      Handler<Context> paramHandler = paramHandlers.get(param);
      if (paramHandler != null) {
        route.addParam(param, paramHandler);
      }
    }

    routes.get(verb).add(route);

  }

  private void addRegEx(Method verb, Pattern regex, Handler<Context> handler) {
    // verify if the binding already exists, if yes add to it
    for (Route route : routes.get(verb)) {
      if (route.isFor(regex)) {
        route.addHandler(verb.name(), handler);
        return;
      }
    }

    final Route route = new Route(regex.pattern(), regex);

    route.addHandler(verb.name(), handler);

    routes.get(verb).add(route);
  }
}
