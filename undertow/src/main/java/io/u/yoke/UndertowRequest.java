package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.impl.AbstractRequest;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Deque;

public class UndertowRequest extends AbstractRequest {

  private final HttpServerExchange exchange;

  // override getRequest state

  private Method method;
  private String path;
  private String query;
  private String uri;

  private Object body;


  public UndertowRequest(@NotNull Context ctx, @NotNull HttpServerExchange exchange) {
    super(ctx, new UndertowHeaders(exchange.getRequestHeaders()));
    this.exchange = exchange;
  }

  @Override
  public void setParam(@NotNull String name, String value) {

  }

  @Override
  protected boolean isSSL() {
    return false;
  }

  @Override
  protected String getRemoteAddress() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getNativeRequest() {
    return (T) exchange;
  }

  @Override
  public <V> V getJSONBody(Class<V> clazz) {
    return null;
  }

  @Override
  public String getVersion() {
    return exchange.getConnection().getTransportProtocol();
  }

  @Override
  public Method getMethod() {
    return Method.valueOf(exchange.getRequestMethod().toString());
  }

  @Override
  public String getURI() {
    return null;
  }

  @Override
  public String getPath() {
    return exchange.getRequestPath();
  }

  @Override
  public String getQuery() {
    return exchange.getQueryString();
  }

  @Override
  public Iterable<String> getParams() {
    return null;
  }

  @Override
  public String getParam(@NotNull String name) {
    // quick escape to avoid undertow to initialize the Deque
    if (exchange.getQueryString().length() == 0) {
      return null;
    }

    final Deque<String> values = exchange.getQueryParameters().get(name);
    if (values != null) {
      return values.peek();
    }

    return null;
  }

  @Override
  public Iterable<String> getParamValues(@NotNull String name) {
    // quick escape to avoid undertow to initialize the Deque
    if (exchange.getQueryString().length() == 0) {
      return Collections.emptySet();
    }

    return exchange.getQueryParameters().get(name);
  }

  @Override
  public void setURI(String val) {
  }

  @Override
  public void setMethod(@NotNull Method method) {

  }

  @Override
  public void setPath(String path) {

  }

  @Override
  public void setQuery(String obj) {

  }
}
