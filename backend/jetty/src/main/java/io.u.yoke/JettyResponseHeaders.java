package io.u.yoke;

import io.u.yoke.http.header.Headers;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

class JettyResponseHeaders implements Headers {

  private final HttpServletResponse res;

  public JettyResponseHeaders(HttpServletResponse res) {
    this.res = res;
  }

  @Override
  public Iterable<String> getHeaders() {
    return res.getHeaderNames();
  }

  @Override
  public String getHeader(@NotNull final String name) {
    return res.getHeader(name);
  }

  @Override
  public Iterable<String> getHeaderValues(@NotNull final String name) {
    return res.getHeaders(name);
  }

  @Override
  public void setHeader(@NotNull String name, String value) {
    res.setHeader(name, value);
  }

  @Override
  public void appendHeader(@NotNull String name, String value) {
    res.addHeader(name, value);
  }

  @Override
  public void removeHeader(@NotNull String name) {
    res.setHeader(name, null);
  }
}
