package io.u.yoke;

import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractResponse;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

public class UndertowResponse extends AbstractResponse {

  private final HttpServerExchange exchange;

  public UndertowResponse(@NotNull Context ctx, @NotNull HttpServerExchange exchange) {
    super(ctx, new UndertowHeaders(exchange.getResponseHeaders()));
    this.exchange = exchange;
  }

  @Override
  protected boolean hasBody() {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getNativeResponse() {
    return (T) exchange;
  }

  @Override
  public Status getStatus() {
    return null;
  }

  @Override
  public void setStatus(Status code) {
    exchange.setResponseCode(code.getCode());
  }

  @Override
  public String getMessage() {
    return null;
  }

  @Override
  public void setMessage(String msg) {
  }

  @Override
  public void setLength(long n) {
    exchange.setResponseContentLength(n);
  }

  @Override
  public long getLength() {
    return 0;
  }

  @Override
  public boolean isHeaderSent() {
    return false;
  }

  @Override
  public void vary(String field) {

  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public Object is(String... types) {
    return null;
  }

  @Override
  public void end() {
    exchange.getResponseSender().close();
  }

  @Override
  public void end(String chunk) {
    exchange.getResponseSender().send(chunk);
  }

  @Override
  public void binary(byte[] chunk) {
    exchange.getResponseSender().send(ByteBuffer.wrap(chunk));
  }

  @Override
  public void sendFile(String file) {
    try {
      // Opens a resource from the current class' defining class loader
      final URL url = getClass().getClassLoader().getResource(file);
      exchange.getResponseChannel().transferFrom( /* in.getChannel()*/ null, 0, 0);
    } catch (IOException e) {
      ctx.fail(Status.INTERNAL_SERVER_ERROR);
    }
  }
}
