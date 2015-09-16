package io.u.yoke;

import io.u.yoke.impl.AbstractYoke;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JettyYoke extends AbstractYoke {

  private Server server;

  public JettyYoke() {
  }

  @Override
  public void listen(int port) {
    try {
      server = new Server(port);

      server.setHandler(new AbstractHandler() {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
          final JettyContext ctx = new JettyContext(locals, baseRequest, req, res);

          // add x-powered-by header is enabled
          Boolean poweredBy = ctx.getAt("x-powered-by");
          if (poweredBy != null && poweredBy) {
            ctx.set("X-Powered-By", "yoke");
          }

          ctx.setIterator(handlers, getErrorHandler());
          // start the handling
          ctx.next();
        }
      });

      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}