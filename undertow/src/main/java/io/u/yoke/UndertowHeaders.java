package io.u.yoke;

import io.u.yoke.http.header.Headers;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

class UndertowHeaders implements Headers {

  private final HeaderMap headers;

  UndertowHeaders(HeaderMap headers) {
    this.headers = headers;
  }

  @Override
  public Iterable<String> getHeaders() {
    return headers.getHeaderNames().stream().map(HttpString::toString).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public String getHeader(@NotNull String name) {
    return headers.getFirst(name);
  }

  @Override
  public Iterable<String> getHeaderValues(@NotNull String name) {
    return headers.get(name);
  }

  @Override
  public void setHeader(@NotNull String name, String value) {
    headers.put(new HttpString(name), value);
  }

  @Override
  public void appendHeader(@NotNull String name, String value) {
    headers.add(new HttpString(name), value);
  }

  @Override
  public void removeHeader(@NotNull String name) {
    headers.remove(name);
  }
}
