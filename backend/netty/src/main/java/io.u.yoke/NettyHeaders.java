package io.u.yoke;

import io.netty.handler.codec.http.HttpHeaders;
import io.u.yoke.http.header.Headers;
import org.jetbrains.annotations.NotNull;

class NettyHeaders implements Headers {

  private final HttpHeaders headers;

  public NettyHeaders(HttpHeaders headers) {
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
    headers.remove(name);
    headers.add(name, value);
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
