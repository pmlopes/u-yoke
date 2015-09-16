package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.Version;
import io.u.yoke.http.impl.AbstractRequest;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

final class JettyRequest extends AbstractRequest {

  private final HttpServletRequest req;

  // override request state

  private Method method;
  private String path;
  private String query;
  private String uri;

  JettyRequest(JettyContext ctx, HttpServletRequest req) {
    super(ctx, new JettyRequestHeaders(req));
    this.req = req;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getNativeRequest() {
    return (T) req;
  }

  @Override
  public <V> V getJSONBody(Class<V> clazz) {
    if (getBody() == null) {
      return null;
    }

//    try {
//      return JSON.decode(((Buffer) getBody()).getBytes(), clazz);
//    } catch (IOException | RuntimeException e) {
//      ctx.fail(Status.BAD_REQUEST);
//      return null;
//    }
    return null;
  }

  @Override
  public Version getVersion() {
    switch (req.getProtocol()) {
      case "HTTP/2.0":
        return Version.HTTP_2_0;
      case "HTTP/1.1":
        return Version.HTTP_1_1;
      case "HTTP/1.0":
      default:
        return Version.HTTP_1_0;
    }
  }

  @Override
  public Method getMethod() {
    if (method != null) {
      return method;
    }

    return Method.valueOf(req.getMethod());
  }

  @Override
  public String getURI() {
    if (uri != null) {
      return uri;
    }
    return req.getRequestURI();
  }

  @Override
  public String getPath() {
    if (path != null) {
      return path;
    }
    return req.getPathInfo();
  }

  @Override
  public String getQuery() {
    if (query != null) {
      return query;
    }

    return req.getQueryString();
  }

  @Override
  public Iterable<String> getParams() {
    return () -> new Iterator<String>() {
      private final Enumeration<String> enumeration = req.getAttributeNames();

      @Override
      public boolean hasNext() {
        return enumeration.hasMoreElements();
      }

      @Override
      public String next() {
        return enumeration.nextElement();
      }
    };
  }

  @Override
  public String getParam(@NotNull final String name) {
    return req.getParameter(name);
  }

  @Override
  public void setParam(@NotNull final String name, final String value) {
//    if (parameters == null) {
//      parameters = decoder.parameters();
//    }
//
//    List<String> parameter = parameters.get(name);
//
//    if (parameter == null) {
//      parameter = new LinkedList<>();
//      parameters.put(name, parameter);
//    }
//
//    parameter.add(value);
  }

  @Override
  public Iterable<String> getParamValues(@NotNull final String name) {
    return Arrays.asList(req.getParameterValues(name));
  }

  @Override
  public void setURI(String uri) {
    this.uri = uri;
  }

  @Override
  public void setMethod(@NotNull final Method method) {
    this.method = method;
  }

  @Override
  public void setPath(String newPath) {
    path = newPath;
  }

  @Override
  public void setQuery(String newQuery) {
    query = newQuery;
  }

  @Override
  protected boolean isSSL() {
//    return req.netSocket().isSsl();
    return false;
  }

  @Override
  protected String getRemoteAddress() {
//    return req.remoteAddress().toString();
    return null;
  }
}
