package io.u.yoke;


import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractResponse;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

final class VertxResponse extends AbstractResponse {

  private final io.vertx.core.Context context;

  private final HttpServerResponse res;
  private final Context ctx;

  private boolean hasBody;

  VertxResponse(Context ctx, HttpServerResponse res) {
    super(ctx, new VertxHeaders(res.headers()));

    context = Vertx.currentContext();

    this.res = res;
    this.ctx = ctx;
    setStatus(Status.OK);
    hasBody = false;
  }

  @Override
  protected boolean hasBody() {
    return hasBody;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getNativeResponse() {
    return (T) res;
  }

  @Override
  public Status getStatus() {
    return Status.valueOf(res.getStatusCode());
  }

  @Override
  public void setStatus(Status code) {
    res.setStatusCode(code.getCode());
    res.setStatusMessage(code.getDescription());
  }

  @Override
  public String getMessage() {
    return res.getStatusMessage();
  }

  @Override
  public void setMessage(String msg) {
    res.setStatusMessage(msg);
  }

  @Override
  public void setLength(long n) {

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
    triggerHeadersHandlers();
    res.end();
    triggerEndHandlers();
  }

  @Override
  public void end(String chunk) {
    if (chunk == null) {
      end();
      return;
    }

    hasBody = true;
    // this call can come from other thread,
    // so we are homing the run in the right context
    context.runOnContext(v -> {
      triggerHeadersHandlers();
      res.end(chunk);
      triggerEndHandlers();
    });
  }

  @Override
  public void binary(byte[] chunk) {
    if (chunk == null) {
      end();
      return;
    }

    hasBody = true;
    // this call can come from other thread,
    // so we are homing the run in the right context
    context.runOnContext(v -> {
      triggerHeadersHandlers();
      res.end(Buffer.buffer(chunk));
      triggerEndHandlers();
    });
  }

  @Override
  public void sendFile(String file) {
    res.sendFile(file);
  }
}
