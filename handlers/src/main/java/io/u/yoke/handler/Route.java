package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.impl.AbstractRequest;
import io.u.yoke.impl.AbstractContext;
import io.u.yoke.jmx.JMXHandler;
import io.u.yoke.jmx.JMXHandlerMBean;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Route implements Handler<Context> {

  private final String route;
  private final Pattern pattern;
  private final Set<String> paramNames;

  private final List<JMXHandlerMBean<Context>> handlers = new ArrayList<>();

  private int params = 0;

  public Route(@NotNull String mount, @NotNull Pattern pattern, Set<String> params) {
    this.route = mount;
    this.pattern = pattern;
    this.paramNames = params;
  }

  public Route(@NotNull String mount, @NotNull Pattern pattern) {
    this.route = mount;
    this.pattern = pattern;
    this.paramNames = Collections.emptySet();
  }

  public boolean isFor(@NotNull String route) {
    return this.route.equals(route);
  }

  public boolean isFor(@NotNull Pattern regex) {
    return pattern.pattern().equals(regex.pattern());
  }

  public void addHandler(@NotNull String verb, Handler<Context> handler) {
    handlers.add(new JMXHandler<>(handlers, route, null, verb, handler));
  }

  public void addParam(String param, Handler<Context> handler) {
    if (paramNames.contains(param)) {
      handlers.add(params++, new JMXHandler<>(handlers, route, null, "ALL", handler));
    }
  }

  @Override
  public void handle(Context ctx) {
    final AbstractContext abstractContext = (AbstractContext) ctx;
    final AbstractRequest request = (AbstractRequest) ctx.request();

    final Matcher m = pattern.matcher(ctx.getPath());

    if (!m.matches()) {
      ctx.next();
      return;
    }

    // first need to process params
    if (paramNames != null) {
      // there are named params
      for (String param : paramNames) {
        request.setParam(param, m.group(param));
      }
    } else {
      // Un-named params
      for (int i = 0; i < m.groupCount(); i++) {
        request.setParam("param" + i, m.group(i + 1));
      }
    }

    // normal flow handling
    abstractContext.setIterator(handlers);
    ctx.next();
  }
}
