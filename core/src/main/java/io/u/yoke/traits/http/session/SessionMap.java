package io.u.yoke.traits.http.session;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class SessionMap<K, V> implements Map<K, V> {

  private final Map<K, V> delegate;
  private int version = 0;

  public SessionMap(Map<K, V> delegate) {
    this.delegate = delegate;
  }

  public SessionMap() {
    this(new LinkedHashMap<>());
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return delegate.get(key);
  }

  @Override
  public V put(K key, V value) {
    version++;
    return delegate.put(key, value);
  }

  @Override
  public V remove(Object key) {
    version++;
    return delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    version++;
    delegate.putAll(m);
  }

  @Override
  public void clear() {
    version++;
    delegate.clear();
  }

  @NotNull
  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @NotNull
  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @NotNull
  @Override
  public Set<Entry<K, V>> entrySet() {
    return delegate.entrySet();
  }

  public boolean isModified() {
    return version != 0;
  }
}
