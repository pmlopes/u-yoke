package io.u.yoke;

import io.u.yoke.http.header.Headers;
import io.vertx.core.MultiMap;
import org.jetbrains.annotations.NotNull;

class VertxHeaders implements Headers {

  private final MultiMap headers;

  public VertxHeaders(MultiMap headers) {
    this.headers = headers;
  }

  @Override
  public Iterable<String> getHeaders() {
    return headers.names();
  }

  @Override
  public String getHeader(@NotNull final String name) {
    return headers.get(name);
  }

  @Override
  public Iterable<String> getHeaderValues(@NotNull final String name) {
    return headers.getAll(name);
  }

  @Override
  public void setHeader(@NotNull String name, String value) {
    headers.set(name, value);
  }

  @Override
  public void appendHeader(@NotNull String name, String value) {
    headers.add(name, value);
  }

  @Override
  public void removeHeader(@NotNull String name) {
    headers.remove(name);
  }
}
