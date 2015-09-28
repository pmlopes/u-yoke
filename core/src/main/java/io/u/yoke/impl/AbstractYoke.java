/**
 * Copyright 2011-2015 the original author or authors.
 */
package io.u.yoke.impl;

import io.u.yoke.*;
import io.u.yoke.jmx.JMXHandlerMBean;
import io.u.yoke.jmx.JMXHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractYoke implements Yoke {

  /**
   * default context values used by all requests
   * <p/>
   * <pre>
   * {
   *   title: "Yoke",
   *   x-powered-by: true,
   *   trust-proxy: true
   * }
   * </pre>
   */
  protected final Map<String, Object> locals = new HashMap<>();

  /**
   * Ordered list of mounted engine in the chain
   */
  protected final List<JMXHandlerMBean<Context>> handlers = new ArrayList<>();

  /**
   * Special engine used for error handling
   */
  private ErrorHandler<Context> errorHandler = new DefaultErrorHandler();

  /**
   * Adds a JMXHandler to the chain. If the engine is an Error JMXHandler JMXHandler then it is
   * treated differently and only the last error engine is kept.
   * <p/>
   * You might want to add a engine that is only supposed to run on a specific route (getPath prefix).
   * In this case if the getRequest getPath does not match the prefix the engine is skipped automatically.
   * <p/>
   * <pre>
   * yoke.use("/login", new CustomLoginMiddleware());
   * </pre>
   *
   * @param mount The route prefix
   * @param handler The engine add to the chain
   */
  @Override
  public Yoke use(@NotNull String mount, @NotNull Handler<Context> handler) {
    handlers.add(new JMXHandler<>(handlers, mount, mount, "ALL", handler));
    return this;
  }

  /**
   * Set the default error engine
   *
   * @param handler the error engine
   * @return self
   */
  @Override
  public Yoke setErrorHandler(@NotNull ErrorHandler<Context> handler) {
    errorHandler = handler;
    return this;
  }

  /**
   * When you need to share global properties with your requests you can add them
   * to Yoke and on every getRequest they will be available as getRequest.get(String)
   *
   * @param key   unique identifier
   * @param value Any non null value, nulls are not saved
   */
  @Override
  public Yoke putAt(@NotNull String key, Object value) {
    if (value == null) {
      locals.remove(key);
    } else {
      locals.put(key, value);
    }

    return this;
  }

  @Override
  public void clear() {
    locals.clear();
    handlers.clear();
  }

  public ErrorHandler<Context> getErrorHandler() {
    return errorHandler;
  }
}
