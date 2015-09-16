package io.u.yoke;

import io.u.yoke.http.header.Headers;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;

class JettyRequestHeaders implements Headers {

  private final HttpServletRequest req;

  public JettyRequestHeaders(HttpServletRequest req) {
    this.req = req;
  }

  @Override
  public Iterable<String> getHeaders() {
    return () -> {
      final Enumeration<String> enumeration = req.getHeaderNames();

      return new Iterator<String>() {
        @Override
        public boolean hasNext() {
          return enumeration.hasMoreElements();
        }

        @Override
        public String next() {
          return enumeration.nextElement();
        }
      };
    };
  }

  @Override
  public String getHeader(@NotNull final String name) {
    return req.getHeader(name);
  }

  @Override
  public Iterable<String> getHeaderValues(@NotNull final String name) {
    return () -> {
      final Enumeration<String> enumeration = req.getHeaders(name);

      return new Iterator<String>() {
        @Override
        public boolean hasNext() {
          return enumeration.hasMoreElements();
        }

        @Override
        public String next() {
          return enumeration.nextElement();
        }
      };
    };
  }

  @Override
  public void setHeader(@NotNull String name, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void appendHeader(@NotNull String name, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeHeader(@NotNull String name) {
    throw new UnsupportedOperationException();
  }
}
