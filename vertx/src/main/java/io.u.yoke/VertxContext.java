package io.u.yoke;

import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.http.Status;
import io.u.yoke.impl.AbstractContext;
import io.u.yoke.impl.AbstractYoke;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class VertxContext extends AbstractContext {

  private final Vertx vertx;
  private final Request request;
  private final Response response;


  VertxContext(@NotNull Vertx vertx, @NotNull Map<String, Object> appLocals, @NotNull HttpServerRequest request) {
    super(appLocals);

    this.vertx = vertx;
    this.request = new VertxRequest(this, request);
    this.response = new VertxResponse(this, request.response());
  }

  @Override
  public Request getRequest() {
    return request;
  }

  @Override
  public Response getResponse() {
    return response;
  }

  // extras Vert.x specific
  public Vertx getVertx() {
    return vertx;
  }
}
