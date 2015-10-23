package io.u.yoke.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.u.yoke.Context;
import io.u.yoke.Engine;
import io.u.yoke.Handler;
import io.u.yoke.http.header.Headers;
import io.u.yoke.http.Method;
import io.u.yoke.http.Response;
import io.u.yoke.http.Status;
import io.u.yoke.json.JSON;
import io.u.yoke.util.HTTPEncode;
import io.u.yoke.util.MimeType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static io.u.yoke.http.Status.*;

public abstract class AbstractResponse extends Common implements Response {

  private static final Map<String, Engine> ENGINE = new HashMap<>();
  private static final String DEFAULT_ENGINE;

  static {
    ServiceLoader<Engine> ldr = ServiceLoader.load(Engine.class);
    String tmpDefaultEngineExtension = null;

    for (Engine engine : ldr) {
      ENGINE.put(engine.getExtension(), engine);
      tmpDefaultEngineExtension = engine.getExtension();
    }

    if (ENGINE.size() == 1) {
      DEFAULT_ENGINE = tmpDefaultEngineExtension;
    } else {
      DEFAULT_ENGINE = null;
    }
  }


  private static final List<Status> REDIRECT_STATUSES = Arrays.asList(
      MULTIPLE_CHOICES,
      MOVED_PERMANENTLY,
      FOUND,
      SEE_OTHER,
      USE_PROXY,
      TEMPORARY_REDIRECT,
      PERMANENT_REDIRECT
  );

  protected final Context ctx;

  // extra handlers
  private Deque<Handler<Void>> headersHandler;
  private boolean headersHandlerTriggered;
  private Deque<Handler<Void>> endHandler;

  public AbstractResponse(@NotNull final Context ctx, @NotNull final Headers headers) {
    super(headers);
    this.ctx = ctx;
  }

  protected abstract boolean hasBody();

  @Override
  public void redirect(String url, String alt) {
    // location
    if ("back".equals(url)) {
      url = getHeader("Referrer");
      if (url == null) {
        url = alt;
      }
      if (url == null) {
        url = "/";
      }
    }

    setHeader("Location", url);

    // status
    if (!REDIRECT_STATUSES.contains(getStatus())) {
      setStatus(FOUND);
    }

    // html
    if (ctx.accepts("html") != null) {
      url = HTTPEncode.encodeURIComponent(url);
      setType("text/html; charset=utf-8");
      end("Redirecting to <a href=\"" + url + "\">" + url + "</a>.");
      return;
    }

    // text
    setType("text/plain; charset=utf-8");
    end("Redirecting to " + url + ".");
  }

  @Override
  public void attachment(String filename) {
    if (filename != null) {
      setType(MimeType.getMime(filename));
    }
    setHeader("Content-Disposition", "attachment; filename=" + filename);
  }

  @Override
  public void setType(String type) {
    setHeader("Content-Type", type);
  }

  @Override
  public void json(Object bean) {
    if (bean == null) {
      end();
    } else {
      try {
        final String encoded = JSON.encode(bean);
        setType("application/json; charset=utf-8");
        end(encoded);
      } catch (JsonProcessingException | RuntimeException e) {
        ctx.fail(INTERNAL_SERVER_ERROR, e);
      }
    }
  }

  private static final Pattern NOQUOTES = Pattern.compile("^(W/)?\"");

  @Override
  public void setEtag(String val) {
    if (!NOQUOTES.matcher(val).matches()) {
      setHeader(Headers.ETAG, "\"" + val + "\"");
    } else {
      setHeader(Headers.ETAG, val);
    }
  }

  @Override
  public void headersHandler(Handler<Void> handler) {
    if (!headersHandlerTriggered) {
      if (headersHandler == null) {
        headersHandler = new LinkedList<>();
      }
      headersHandler.push(handler);
    }
  }

  @Override
  public void endHandler(Handler<Void> handler) {
    if (endHandler == null) {
      endHandler = new LinkedList<>();
    }
    endHandler.push(handler);
  }

  protected void triggerHeadersHandlers() {
    if (!headersHandlerTriggered) {
      headersHandlerTriggered = true;
      // if there are handlers call them
      if (headersHandler != null) {
        Handler<Void> handler;
        while ((handler = headersHandler.pollFirst()) != null) {
          handler.handle();
        }
      }

      // convert the cookies
      for (HttpCookie cookie : getCookies()) {
        appendHeader(Headers.SET_COOKIE, ServerCookieEncoder.encode(cookie));
      }

//      // if there is a filter then putAt the right get
//      if (filter != null) {
//        // verify if the filter can filter this content
//        if (filter.canFilter(getResponse.getHeaders().get("content-type"))) {
//          getResponse.putHeader("content-encoding", filter.encoding());
//        } else {
//          // disable the filter
//          filter = null;
//        }
//      }

      // if there is no content and method is not HEAD delete content-type, content-encoding
      if (!hasBody() && ctx.getRequest().getMethod() != Method.HEAD) {
        removeHeader("Content-Encoding");
        removeHeader("Content-Type");
      }
    }
  }

  protected void triggerEndHandlers() {
    if (endHandler != null) {
      Handler<Void> handler;
      while ((handler = endHandler.pollFirst()) != null) {
        handler.handle(null);
      }
    }
  }

  @Override
  public void render(@NotNull final String template) {
    int sep = template.lastIndexOf('.');
    if (sep != -1) {
      String extension = template.substring(sep);

      final Engine renderEngine = ENGINE.get(extension);

      if (renderEngine == null) {
        ctx.fail("No engine registered for extension: " + extension);
      } else {
        renderEngine.render(template, ctx);
      }
    } else {
      // when no extension is provided but only 1 engine is available assume that one
      if (DEFAULT_ENGINE != null) {
        final Engine renderEngine = ENGINE.get(DEFAULT_ENGINE);
        renderEngine.render(template, ctx);
      } else {
        ctx.fail("Cannot determine the extension of the template");
      }
    }
  }
}
