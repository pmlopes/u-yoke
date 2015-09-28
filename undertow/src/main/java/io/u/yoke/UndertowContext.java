package io.u.yoke;

import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.impl.AbstractContext;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class UndertowContext extends AbstractContext {

  private final Request request;
  private final Response response;

  public UndertowContext(@NotNull Map<String, Object> yokeLocals, HttpServerExchange exchange) {
    super(yokeLocals);

    request = new UndertowRequest(this, exchange);
    response = new UndertowResponse(this, exchange);
  }

  @Override
  public Request getRequest() {
    return request;
  }

  @Override
  public Response getResponse() {
    return response;
  }
}
