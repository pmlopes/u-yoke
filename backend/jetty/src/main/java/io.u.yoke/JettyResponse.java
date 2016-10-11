package io.u.yoke;


import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class JettyResponse extends AbstractResponse {

  private final HttpServletResponse res;

  private boolean hasBody;

  JettyResponse(JettyContext ctx, HttpServletResponse res) {
    super(ctx, new JettyResponseHeaders(res));
    System.out.println(Thread.currentThread());
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
    _end();
    triggerEndHandlers();
  }

  @Override
  public void end(String chunk) {
    System.out.println(Thread.currentThread());

    if (chunk == null) {
      end();
      return;
    }

    if (getHeader(CONTENT_TYPE) == null) {
      setHeader(CONTENT_TYPE, "text/plain");
    }

    res.setContentType(getHeader(CONTENT_TYPE));

    // TODO: respect character encoding header
    byte[] data = chunk.getBytes(StandardCharsets.UTF_8);

    if (getHeader(CONTENT_LENGTH) == null) {
      res.setContentLength(data.length);
    }

    _end();

    hasBody = true;
    triggerHeadersHandlers();

    try {
      res.getOutputStream().write(data);
      res.getOutputStream().close();
    } catch (IOException e) {
      e.printStackTrace();
      //throw new RuntimeException(e);
    }

    triggerEndHandlers();
  }

  @Override
  public void binary(byte[] chunk) {
    if (chunk == null) {
      end();
      return;
    }

    if (getHeader(CONTENT_TYPE) == null) {
      setHeader(CONTENT_TYPE, "application/octet-stream");
    }

    if (getHeader(CONTENT_LENGTH) == null) {
      setHeader(CONTENT_LENGTH, Integer.toString(chunk.length));
    }

    _end();

    hasBody = true;
    triggerHeadersHandlers();

    try {
      res.getOutputStream().write(chunk);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void _end() {
    final boolean success = res.getStatus() < 400;
    final boolean keepAlive = success && ctx.getRequest().isKeepAlive();

    if (keepAlive) {
      setHeader(CONNECTION, "keep-alive");
    }
  }

  @Override
  public void sendFile(String file) {
//    res.sendFile(file);
  }
}
