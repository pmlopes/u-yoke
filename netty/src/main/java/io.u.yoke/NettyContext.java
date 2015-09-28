package io.u.yoke;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.impl.AbstractContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class NettyContext extends AbstractContext {

  private final Request request;
  private final Response response;

  NettyContext(@NotNull ChannelHandlerContext nettyContext, @NotNull Map<String, Object> appLocals, @NotNull HttpRequest req) {
    super(appLocals);

    this.request = new NettyRequest(this, req);
    this.response = new NettyResponse(this, nettyContext, new DefaultFullHttpResponse(req.getProtocolVersion(), HttpResponseStatus.OK));
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
