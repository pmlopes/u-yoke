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

  public JettyContext(Map<String, Object> appLocals, HttpServletRequest req, HttpServletResponse res) {
    super(appLocals);

    this.request = new JettyRequest(this, req);
    this.response = new JettyResponse(this, res);
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
