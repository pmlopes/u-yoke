/**
 * Copyright 2011-2015 the original author or authors.
 */
package io.u.yoke;

import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public interface Yoke {

  static Yoke getDefault() {
    ServiceLoader<Yoke> ldr = ServiceLoader.load(Yoke.class);
    for (Yoke provider : ldr) {
      // TODO: handle more than 1
      return provider;
    }
    throw new Error("No Yoke provider registered");
  }

  /**
   * Adds a Handler to the chain. If the engine is an Error JMXHandler JMXHandler then it is
   * treated differently and only the last error engine is kept.
   * <p/>
   * You might want to add a engine that is only supposed to run on a specific route (path prefix).
   * In this case if the getRequest getPath does not match the prefix the engine is skipped automatically.
   * <p/>
   * <pre>
   * yoke.use(new CustomLoginMiddleware());
   * </pre>
   *
   * @param mount The mount prefix
   * @param handler The engine add to the chain
   */
  Yoke use(@NotNull String mount, @NotNull Handler<Context> handler);

  default Yoke use(@NotNull Handler<Context> handler) {
    return use("/", handler);
  }

  /**
   * Installs a error engine
   *
   * @param handler the error engine
   * @return self.
   */
  Yoke setErrorHandler(@NotNull ErrorHandler<Context> handler);

  /**
   * When you need to share global properties with your requests you can add them
   * to Yoke and on every getRequest they will be available as getRequest.get(String)
   *
   * @param key   unique identifier
   * @param value Any non null value, nulls are not saved
   */
  Yoke putAt(@NotNull String key, Object value);

  /**
   * Removes all Handlers in this instance.
   */
  void clear();

  /**
   * Closes this server.
   */
  void close();

  /**
   * Starts the server.
   */
  void listen(int port);
}
