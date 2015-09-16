
package io.u.yoke.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * A simple {@link Handler<Context>} that allows adding custom response headers to all
 * {@link io.u.yoke.http.Response}.
 * </p>
 * <p>
 * Example:
 * </p>
 * <p>
 * <pre>
 * {@code
 * yoke.use(new ResponseHeaders()
 *          .with("X-Build-Meta", "1.0-SNAPSHOT")
 *          .with("X-Server-Id", "server-123"));
 * }
 * </pre>
 */
public class ResponseHeaders implements Handler<Context> {
  private final Map<String, String[]> headers;

  public ResponseHeaders() {
    headers = new HashMap<>();
  }

  public ResponseHeaders with(final String name, final String... values) {
    headers.put(name, values);
    return this;
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    for (final Entry<String, String[]> header : headers.entrySet()) {
      for (String value : header.getValue()) {
        ctx.response().appendHeader(header.getKey(), value);
      }
    }

    ctx.next();
  }
}
