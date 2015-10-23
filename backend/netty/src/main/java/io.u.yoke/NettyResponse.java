package io.u.yoke;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractResponse;

final class NettyResponse extends AbstractResponse {

  private final FullHttpResponse res;
  private final ChannelHandlerContext ctx;

  private boolean hasBody;

  NettyResponse(NettyContext ctx, ChannelHandlerContext nettyContext, FullHttpResponse res) {
    super(ctx, new NettyHeaders(res.headers()));

    this.res = res;
    this.ctx = nettyContext;
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
    return Status.valueOf(res.getStatus().code());
  }

  @Override
  public void setStatus(Status code) {
    res.setStatus(HttpResponseStatus.valueOf(code.getCode()));
  }

  @Override
  public String getMessage() {
    return res.getStatus().reasonPhrase();
  }

  @Override
  public void setMessage(String msg) {
    res.setStatus(new HttpResponseStatus(res.getStatus().code(), msg));
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
    _end();
    triggerEndHandlers();
  }

  @Override
  public void end(String chunk) {
    if (chunk == null) {
      end();
      return;
    }

    hasBody = true;
    triggerHeadersHandlers();

    res.content()
        .writeBytes(chunk.getBytes());

    _end();
    triggerEndHandlers();
  }

  @Override
  public void binary(byte[] chunk) {
    if (chunk == null) {
      end();
      return;
    }

    hasBody = true;
    triggerHeadersHandlers();

    res.content()
        .writeBytes(chunk);

    _end();
    triggerEndHandlers();
  }

  private void _end() {

    final HttpHeaders headers = res.headers();

    if (!headers.contains(CONTENT_TYPE)) {
      headers.set(CONTENT_TYPE, "text/plain");
    }

    if (!headers.contains(CONTENT_LENGTH)) {
      headers.set(CONTENT_LENGTH, res.content().readableBytes());
    }

    final boolean success = res.getStatus().code() < 400;
    final boolean keepAlive = success && HttpHeaders.isKeepAlive(super.ctx.getRequest().getNativeRequest());

    if (!keepAlive) {
      ctx
          .write(res)
          .addListener(ChannelFutureListener.CLOSE);
    } else {
      headers.set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      ctx
          .write(res);
    }
  }

  @Override
  public void sendFile(String file) {
//    res.sendFile(file);
  }
}
