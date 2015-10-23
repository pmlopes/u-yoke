package io.u.yoke;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.u.yoke.http.Method;
import io.u.yoke.http.impl.AbstractRequest;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class NettyRequest extends AbstractRequest {

  private final HttpRequest req;

  private final QueryStringDecoder decoder;
  // override getRequest state

  private Method method;
  private String path;
  private String query;
  private String uri;
  private Map<String, List<String>> parameters;

  NettyRequest(NettyContext ctx, HttpRequest req) {
    super(ctx, new NettyHeaders(req.headers()));

    this.req = req;
    this.decoder = new QueryStringDecoder(req.getUri());
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
  public String getVersion() {
    return req.getProtocolVersion().text();
  }

  @Override
  public Method getMethod() {
    if (method != null) {
      return method;
    }

    return Method.valueOf(req.getMethod().name());
  }

  @Override
  public String getURI() {
    if (uri != null) {
      return uri;
    }
    return decoder.uri();
  }

  @Override
  public String getPath() {
    if (path != null) {
      return path;
    }
    return decoder.path();
  }

  @Override
  public String getQuery() {
    if (query != null) {
      return query;
    }

    return req.getUri().substring(getPath().length() + 1);
  }

  @Override
  public Iterable<String> getParams() {
    if (parameters == null) {
      parameters = decoder.parameters();
    }
    return parameters.keySet();
  }

  @Override
  public String getParam(@NotNull final String name) {
    if (parameters == null) {
      parameters = decoder.parameters();
    }

    final List<String> list = parameters.get(name);
    if (list == null || list.size() == 0) {
      return null;
    }

    return list.get(0);
  }

  @Override
  public void setParam(@NotNull final String name, final String value) {
    if (parameters == null) {
      parameters = decoder.parameters();
    }

    List<String> parameter = parameters.get(name);

    if (parameter == null) {
      parameter = new LinkedList<>();
      parameters.put(name, parameter);
    }

    parameter.add(value);
  }

  @Override
  public Iterable<String> getParamValues(@NotNull final String name) {
    if (parameters == null) {
      parameters = decoder.parameters();
    }
    return parameters.get(name);
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
