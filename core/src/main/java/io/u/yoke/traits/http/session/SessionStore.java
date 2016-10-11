package io.u.yoke.traits.http.session;

import io.u.yoke.Handler;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public interface SessionStore {

  static SessionStore getDefault() {
    ServiceLoader<SessionStore> ldr = ServiceLoader.load(SessionStore.class);
    SessionStore provider = null;
    for (SessionStore impl : ldr) {
      if (provider == null) {
        provider = impl;
      } else {
        throw new Error("More than one provider registered!");
      }
    }

    if (provider == null) {
      System.err.println("No SessionStore provider registered");
    }

    return provider;
  }

  // Attempt to fetch session by the given `sid`.
  void get(String sid, Handler<Map<String, ?>> callback);

  // Commit the given `sess` object associated with the given `sid`.
  void set(String sid, Map<String, ?> sess, Handler<Object> callback);

  // Destroy the session associated with the given `sid`.
  void destroy(String sid, Handler<Object> callback);

  // Invoke the given callback `fn` with all active sessions.
  void all(Handler<List<Map<String, ?>>> callback);

  // Clear all sessions.
  void clear(Handler<Object> callback);

  // Fetch number of sessions.
  void length(Handler<Integer> callback);
}
