package io.u.yoke;

import io.u.yoke.impl.AbstractYoke;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class VertxYoke extends AbstractYoke {

  private final Vertx vertx;
  private final HttpServer server;

  public VertxYoke() {
    vertx = Vertx.vertx();
    server = vertx.createHttpServer();
  }

  @Override
  public void listen(int port) {
    final CompletableFuture<AsyncResult<?>> ready = new CompletableFuture<>();

    server.requestHandler(request -> {

      final VertxContext ctx = new VertxContext(vertx, locals, request);

      // add x-powered-by header is enabled
      Boolean poweredBy = ctx.getAt("x-powered-by");
      if (poweredBy != null && poweredBy) {
        ctx.set("X-Powered-By", "yoke");
      }

      ctx.setIterator(handlers, getErrorHandler());
      // start the handling
      ctx.next();
    });

    server.listen(port, ready::complete);

    try {
      AsyncResult<?> result = ready.get();
      if (result.failed()) {
        throw new RuntimeException(result.cause());
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    server.close();
  }
}