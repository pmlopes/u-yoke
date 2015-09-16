package io.u.yoke;


import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class JettyResponse extends AbstractResponse {

  private final org.eclipse.jetty.server.Request req;
  private final HttpServletResponse res;

  private boolean hasBody;

  JettyResponse(JettyContext ctx, org.eclipse.jetty.server.Request req, HttpServletResponse res) {
    super(ctx, new JettyResponseHeaders(res));
    this.req = req;
    this.res = res;
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
    return Status.valueOf(res.getStatus());
  }

  @Override
  public void setStatus(Status code) {
    res.setStatus(code.getCode());
  }

  @Override
  public String getMessage() {
    return getStatus().getDescription();
  }

  @Override
  public void setMessage(String msg) {
//    res.setStatus(new HttpResponseStatus(res.getStatus().code(), msg));
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
    req.setHandled(true);
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

    try {
      res.getWriter().write(chunk);
      req.setHandled(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    _end();
    triggerEndHandlers();
  }

  @Override
  public void binary(byte[] chunk) {
//    if (chunk == null) {
//      end();
//      return;
//    }
//
//    hasBody = true;
//    triggerHeadersHandlers();
//
//    res.content()
//        .writeBytes(chunk);
//
//    _end();
//    triggerEndHandlers();
  }

  private void _end() {

//    final HttpHeaders headers = res.headers();
//
//    if (!headers.contains(CONTENT_TYPE)) {
//      headers.set(CONTENT_TYPE, "text/plain");
//    }
//
//    if (!headers.contains(CONTENT_LENGTH)) {
//      headers.set(CONTENT_LENGTH, res.content().readableBytes());
//    }
//
//    final boolean success = res.getStatus().code() < 400;
//    final boolean keepAlive = success && HttpHeaders.isKeepAlive(super.ctx.request().getNativeRequest());
//
//    if (!keepAlive) {
//      ctx
//          .write(res)
//          .addListener(ChannelFutureListener.CLOSE);
//    } else {
//      headers.set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//      ctx
//          .write(res);
//    }
  }

  @Override
  public void sendFile(String file) {
//    res.sendFile(file);
  }
}
