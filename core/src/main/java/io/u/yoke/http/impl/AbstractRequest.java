package io.u.yoke.http.impl;

import io.u.yoke.Context;
import io.u.yoke.http.*;
import io.u.yoke.http.header.Headers;
import io.u.yoke.json.JSON;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class AbstractRequest extends CommonImpl implements Request {

  private static final List<Method> IDEMPOTENT_METHODS = Collections.unmodifiableList(Arrays.asList(
      Method.GET,
      Method.HEAD,
      Method.PUT,
      Method.DELETE,
      Method.OPTIONS,
      Method.TRACE
  ));

  private final Context ctx;

  private Map<String, File> files;
  private long maxLength = -1;
  private Object body;

  public AbstractRequest(@NotNull final Context ctx, @NotNull final Headers headers) {
    super(headers);
    this.ctx = ctx;
  }

  public abstract void setParam(@NotNull String name, String value);

  @Override
  public Iterable<String> getFiles() {
    return files.keySet();
  }

  @Override
  public File getFile(@NotNull String name) {
    return files.get(name);
  }

  @Override
  public void setMaxLength(long limit) {
    maxLength = limit;
  }

  @Override
  public long getMaxLength() {
    return maxLength;
  }

  public <V> void setBody(V body) {
    this.body = body;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getBody() {
    return (V) body;
  }

  @Override
  public Locale locale() {
    String languages = getHeader("Accept-Language");
    if (languages != null) {
      // parse
      String[] acceptLanguages = languages.split(" *, *");
      // sort on quality
      Arrays.sort(acceptLanguages, ACCEPT_X_COMPARATOR);

      String bestLanguage = acceptLanguages[0];

      int idx = bestLanguage.indexOf(';');

      if (idx != -1) {
        bestLanguage = bestLanguage.substring(0, idx).trim();
      }

      String[] parts = bestLanguage.split("_|-");
      switch (parts.length) {
        case 3: return new Locale(parts[0], parts[1], parts[2]);
        case 2: return new Locale(parts[0], parts[1]);
        case 1: return new Locale(parts[0]);
      }
    }

    return Locale.getDefault();
  }


  private static String[] splitMime(@NotNull String mime) {
    // find any ; e.g.: "application/json;q=0.8"
    int space = mime.indexOf(';');

    if (space != -1) {
      mime = mime.substring(0, space);
    }

    String[] parts = mime.split("/");

    if (parts.length < 2) {
      return new String[]{
          parts[0],
          "*"
      };
    }

    return parts;
  }

  private static String acceptsHeader(@NotNull String header, @NotNull String... type) {
    // parse
    String[] acceptTypes = header.split(" *, *");
    // sort on quality
    Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);

    for (String senderAccept : acceptTypes) {
      String[] sAccept = splitMime(senderAccept);

      for (String appAccept : type) {
        String[] aAccept = splitMime(appAccept);

        if ((sAccept[0].equals(aAccept[0]) || "*".equals(sAccept[0]) || "*".equals(aAccept[0])) &&
            (sAccept[1].equals(aAccept[1]) || "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
          return senderAccept;
        }
      }
    }

    return null;
  }

  protected abstract boolean isSSL();

  protected abstract String getRemoteAddress();

  @Override
  public boolean isIdempotent() {
    return IDEMPOTENT_METHODS.contains(getMethod());
  }

  @Override
  public String getCharset() {
    final String type = getHeader("Content-Type");
    if (type == null) {
      return null;
    }

    return Headers.getParameter(type, "charset");
  }

  @Override
  public String accepts(@NotNull String... type) {
    String accept = getHeader("Accept");
    // accept anything when accept is not present
    if (accept == null) {
      return type[0];
    }

    return acceptsHeader(accept, type);
  }

  @Override
  public String acceptsEncodings(@NotNull String... encoding) {
    String header = getHeader("Accept-Encoding");
    // accept anything when accept is not present
    if (header == null) {
      return encoding[0];
    }

    return acceptsHeader(header, encoding);
  }

  @Override
  public String acceptsCharsets(@NotNull String... charset) {
    String header = getHeader("Accept-Charset");
    // accept anything when accept is not present
    if (header == null) {
      return charset[0];
    }

    return acceptsHeader(header, charset);
  }

  @Override
  public String acceptsLanguages(@NotNull String... lang) {
    String header = getHeader("Accept-Charset");
    // accept anything when accept is not present
    if (header == null) {
      return lang[0];
    }

    return acceptsHeader(header, lang);
  }

  @Override
  public String getHost() {
    final Boolean proxy = ctx.getAt("proxy");
    String host = null;

    if (proxy != null && proxy) {
      host = getHeader("X-Forwarded-Host");
    }

    if (host != null) {
      host = getHeader("Host");
    }

    if (host != null) {
      return host.split("\\s*,\\s*")[0];
    }

    return null;
  }

  @Override
  public boolean isFresh() {
    final Method method = getMethod();

    // GET or HEAD for weak freshness validation only
    if (method != Method.GET && method != Method.HEAD) {
      return false;
    }

    // TODO: implement me!
    final int s = 200; //ctx.getStatus();
    // 2xx or 304 as per rfc2616 14.26
    if ((s >= 200 && s < 300) || 304 == s) {
      //return fresh(this.header, this.ctx.response.header);
    }

    return false;
  }

  @Override
  public String getProtocol() {
    final Boolean proxy = ctx.getAt("proxy");

    if (isSSL()) {
      return "https";
    }

    if (proxy == null || !proxy) {
      return "http";
    }

    final String proto = getHeader("X-Forwarded-Proto");

    if (proto == null) {
      return "http";
    }

    return proto.split("\\s*,\\s*")[0];
  }

  @Override
  public Iterable<String> getIps() {
    final Boolean proxy = ctx.getAt("proxy");
    final String val = getHeader("X-Forwarded-For");

    if (proxy != null && proxy && val != null) {
      return Arrays.asList(val.split(" *, *"));
    }

    return Collections.emptyList();
  }

  @Override
  public String getIp() {
    for (final String ip : getIps()) {
      return ip;
    }

    return getRemoteAddress();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Request\n");
    printValue(sb, "version", getVersion().toString());
    printValue(sb, "method", getMethod().toString());
    printValue(sb, "URI", getURI());
    printValue(sb, "path", getPath());
    printValue(sb, "query", getQuery());

    return sb.toString();
  }

  private static void printValue(StringBuffer sb, String what, String value) {
    sb.append("  ");
    sb.append(what);
    sb.append('=');
    sb.append(value != null ? value : "");
    sb.append('\n');
  }
}