package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractRequest;
import io.u.yoke.json.JSON;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

final class JettyRequest extends AbstractRequest {

  private final HttpServletRequest req;

  // override request state

  private Method method;
  private String path;
  private String query;
  private String uri;

  private Map<String, String[]> parameters;

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

    try {
      return JSON.decode(((byte[]) getBody()), clazz);
    } catch (IOException | RuntimeException e) {
      ctx.fail(Status.BAD_REQUEST);
      return null;
    }
  }

  @Override
  public String getVersion() {
    return req.getProtocol();
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
    if (parameters == null) {
      parameters = new LinkedHashMap<>(req.getParameterMap());
    }

    return parameters.keySet();
  }

  @Override
  public String getParam(@NotNull final String name) {
    if (parameters == null) {
      parameters = new LinkedHashMap<>(req.getParameterMap());
    }

    String[] values = parameters.get(name);

    if (values != null && values.length > 0) {
      return values[0];
    }

    return null;
  }

  @Override
  public void setParam(@NotNull final String name, final String value) {
    if (parameters == null) {
      parameters = new LinkedHashMap<>(req.getParameterMap());
    }

    String[] values = parameters.get(name);

    if (values == null) {
      parameters.put(name, new String[] { value });
    } else {
      String[] _values = new String[values.length + 1];
      System.arraycopy(values, 0, _values, 0, values.length);
      _values[values.length] = value;
      parameters.put(name, _values);
    }
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
    return req.isSecure();
  }

  @Override
  protected String getRemoteAddress() {
    return req.getRemoteAddr();
  }
}
