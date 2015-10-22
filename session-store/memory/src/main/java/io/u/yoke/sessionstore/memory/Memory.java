/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.sessionstore.memory;

import io.u.yoke.Handler;
import io.u.yoke.traits.http.session.SessionStore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Memory implements SessionStore {

  private final ConcurrentMap<String, Map<String, ?>> storage = new ConcurrentHashMap<>();

  @Override
  public void get(String sid, Handler<Map<String, ?>> callback) {
    callback.handle(storage.get(sid));
  }

  @Override
  public void set(String sid, Map<String, ?> sess, Handler<Object> callback) {
    storage.put(sid, sess);
    callback.handle(null);
  }

  @Override
  public void destroy(String sid, Handler<Object> callback) {
    storage.remove(sid);
    callback.handle(null);
  }

  @Override
  public void all(Handler<List<Map<String, ?>>> callback) {
    List<Map<String, ?>> items = new LinkedList<>();
    for (Map<String, ?> item : storage.values()) {
      items.add(item);
    }

    callback.handle(items);
  }

  @Override
  public void clear(Handler<Object> callback) {
    storage.clear();
    callback.handle(null);
  }

  @Override
  public void length(Handler<Integer> callback) {
    callback.handle(storage.size());
  }
}
