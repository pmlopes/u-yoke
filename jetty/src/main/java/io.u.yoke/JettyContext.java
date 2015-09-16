package io.u.yoke;

import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.impl.AbstractContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public final class JettyContext extends AbstractContext {

  private final Request request;
  private final Response response;

  public JettyContext(Map<String, Object> appLocals, org.eclipse.jetty.server.Request baseReq, HttpServletRequest req, HttpServletResponse res) {
    super(appLocals);

    this.request = new JettyRequest(this, req);
    this.response = new JettyResponse(this, baseReq, res);
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public Response response() {
    return response;
  }
}
