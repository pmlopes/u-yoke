package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractRequest;
import io.u.yoke.json.JSON;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class VertxRequest extends AbstractRequest {

  private final io.vertx.core.Context context;

  private final HttpServerRequest req;
  private final Context ctx;

  // override getRequest state

  private Method method;
  private String path;
  private String query;
  private String uri;

  VertxRequest(Context ctx, HttpServerRequest req) {
    super(ctx, new VertxHeaders(req.headers()));

    context = Vertx.currentContext();

    this.ctx = ctx;
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
      return JSON.decode(((Buffer) getBody()).getBytes(), clazz);
    } catch (IOException | RuntimeException e) {
      ctx.fail(Status.BAD_REQUEST);
      return null;
    }
  }

  @Override
  public String getVersion() {
    return req.version().name();
  }

  @Override
  public Method getMethod() {
    if (method != null) {
      return method;
    }

    return Method.valueOf(req.method().name());
  }

  @Override
  public String getURI() {
    if (uri != null) {
      return uri;
    }
    return req.absoluteURI();
  }

  @Override
  public String getPath() {
    if (path != null) {
      return path;
    }
    return req.path();
  }

  @Override
  public String getQuery() {
    if (query != null) {
      return query;
    }

    return req.query();
  }

  @Override
  public Iterable<String> getParams() {
    return req.params().names();
  }

  @Override
  public String getParam(@NotNull final String name) {
    return req.getParam(name);
  }

  @Override
  public void setParam(@NotNull final String name, final String value) {
    req.params().set(name, value);
  }

  @Override
  public Iterable<String> getParamValues(@NotNull final String name) {
    return req.params().getAll(name);
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
    return req.netSocket().isSsl();
  }

  @Override
  protected String getRemoteAddress() {
    return req.remoteAddress().toString();
  }
}
