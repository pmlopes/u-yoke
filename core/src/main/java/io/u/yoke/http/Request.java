package io.u.yoke.http;

import io.u.yoke.http.header.Headers;
import io.u.yoke.traits.http.CookieTrait;
import io.u.yoke.traits.http.FileUploadTrait;
import io.u.yoke.traits.http.FormDataTrait;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface Request extends Headers, CookieTrait, FormDataTrait, FileUploadTrait {

  <T> T getNativeRequest();

  // Holds the maximum allowed getLength for the setBody data. -1 for unlimited
  long getMaxLength();

  /** Returns true if this getRequest has setBody
   *
   * @return {Boolean} true if content-getLength or transfer-encoding is present
   */
  default boolean hasBody() {
    final String transferEncoding = getHeader("Transfer-Encoding");
    final String contentLength = getHeader("Content-Length");
    return transferEncoding != null || contentLength != null;
  }

  /** The getRequest body */
  <V> V getBody();

  <V> V getJSONBody(Class<V> clazz);

  default Map getJSONBody() {
    return getJSONBody(Map.class);
  }

  /** Read the default locale for this getRequest
   *
   * @return Locale (best match if more than one)
   */
  Locale locale();

//  String normalizedPath();

  /**
   * The HTTP getVersion of the getRequest
   */
  String getVersion();

  /**
   * The HTTP method for the getRequest. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
   */
  Method getMethod();

//  /**
//   * The Original HTTP method for the getRequest. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
//   */
//  Method getOriginalMethod();

  /**
   * The normalized getURI of the getRequest. For example
   * http://www.somedomain.com/somepath/somemorepath/someresource.foo?someparam=32&amp;someotherparam=x
   */
  String getURI();

  /**
   * The getPath part of the getURI. For example /somepath/somemorepath/someresource.foo
   */
  String getPath();

  /**
   * The getQuery part of the getURI. For example someparam=32&amp;someotherparam=x
   */
  String getQuery();

  /**
   * Set getRequest URI.
   *
   * @param val the overridden value
   */
  void setURI(String val);

  /**
   * Set getRequest getMethod.
   *
   * @param method new setMethod GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
   */
  void setMethod(@NotNull Method method);

  /**
   * Set pathname, retaining the getQuery-string when present.
   *
   * @param path
   */
  void setPath(String path);

  /**
   * Set getQuery-string override the original.
   *
   * @param obj the new getQuery
   */
  void setQuery(String obj);

  /**
   * Parse the "Host" get field host
   * and support X-Forwarded-Host when a
   * proxy is enabled.
   *
   * @return {String} hostname:port
   */
  String getHost();

  /**
   * Parse the "Host" get field hostname
   * and support X-Forwarded-Host when a
   * proxy is enabled.
   *
   * @return {String} hostname
   */
  default String getHostname() {
    final String host = getHost();
    if (host != null) {
      return host.split(":")[0];
    }

    return null;
  }

  /**
   * Check if the getRequest is fresh, aka
   * Last-Modified and/or the ETag
   * still match.
   *
   * @return {Boolean}
   */
  boolean isFresh();

  /**
   * Check if the getRequest is stale, aka
   * "Last-Modified" and / or the "ETag" for the
   * resource has changed.
   *
   * @return {Boolean}
   */
  default boolean isStale() {
    return !isFresh();
  }

  /**
   * Check if the getRequest is idempotent.
   *
   * @return {Boolean}
   */
  boolean isIdempotent();

  /**
   * Get the getCharset when present or undefined.
   *
   * @return {String}
   */
  String getCharset();

  /**
   * Return parsed Content-Length when present.
   *
   * @return {Number}
   */
  default long getLength() {
    String len = getHeader("Content-Length");
    if (null == len) {
      return -1;
    }
    return Long.parseLong(len);
  }

  /**
   * Return the protocol string "http" or "https"
   * when requested with TLS. When the proxy setting
   * is enabled the "X-Forwarded-Proto" get
   * field will be trusted. If you're running behind
   * a reverse proxy that supplies https for you this
   * may be enabled.
   *
   * @return {String}
   */
  String getProtocol();

  /**
   * Short-hand for:
   * <p/>
   * this.protocol == 'https'
   *
   * @return {Boolean}
   */
  default boolean isSecure() {
    return "https".equalsIgnoreCase(getProtocol());
  }

  /**
   * Return the remote address, or when
   * `app.proxy` is `true` return
   * the upstream addr.
   *
   * @return {String}
   */
  String getIp();

  /**
   * When `app.proxy` is `true`, parse
   * the "X-Forwarded-For" ip address list.
   * <p/>
   * For example if the value were "client, proxy1, proxy2"
   * you would receive the array `["client", "proxy1", "proxy2"]`
   * where "proxy2" is the furthest down-stream.
   *
   * @return {Array}
   */
  Iterable<String> getIps();

  /**
   * Return subdomains as an array.
   * <p/>
   * Subdomains are the dot-separated parts of the host before the main domain of
   * the app. By default, the domain of the app is assumed to be the last two
   * parts of the host.
   * <p/>
   * For example, if the domain is "tobi.ferrets.example.com":
   * If `app.subdomainOffset` is not putAt, this.subdomains is `["ferrets", "tobi"]`.
   *
   * @return {Array}
   */
  default Iterable<String> getSubdomains() {
    final String host = getHost();
    if (host == null) {
      return Collections.emptyList();
    }

    final String[] subdomains = host.split("\\.");

    if (subdomains.length > 2) {
      Arrays.sort(subdomains, Collections.reverseOrder());
      final List<String> list = Arrays.asList(subdomains);
      list.remove(subdomains.length - 1);
      list.remove(subdomains.length - 2);

      return list;
    }

    return Collections.emptyList();
  }

  /**
   * Check if the given `type(s)` is acceptable, returning
   * the best match when true, otherwise `undefined`, in which
   * case you should respond with 406 "Not Acceptable".
   * <p/>
   * The `type` value may be a single mime type string
   * such as "application/json", the extension name
   * such as "json" or an array `["json", "html", "text/plain"]`. When a list
   * or array is given the _best_ match, if any is returned.
   * <p/>
   * Examples:
   * <p/>
   * // Accept: text/html
   * this.accepts('html');
   * // => "html"
   * <p/>
   * // Accept: text/*, application/json
   * this.accepts('html');
   * // => "html"
   * this.accepts('text/html');
   * // => "text/html"
   * this.accepts('json', 'text');
   * // => "json"
   * this.accepts('application/json');
   * // => "application/json"
   * <p/>
   * // Accept: text/*, application/json
   * this.accepts('image/png');
   * this.accepts('png');
   * // => null
   * <p/>
   * // Accept: text/*;q=.5, application/json
   * this.accepts(['html', 'json']);
   * this.accepts('html', 'json');
   * // => "json"
   *
   * @param type list of accepted content types
   * @return the best matching accepted type
   */
  String accepts(@NotNull String... type);

  /**
   * Return accepted encodings or best fit based on `encodings`.
   * <p/>
   * Given `Accept-Encoding: gzip, deflate`
   * an array sorted by quality is returned:
   * <p/>
   * ['gzip', 'deflate']
   *
   * @param encoding the wanted encodings
   */
  String acceptsEncodings(@NotNull String... encoding);

  /**
   * Return accepted charsets or best fit based on `charsets`.
   * <p/>
   * Given `Accept-Charset: utf-8, iso-8859-1;q=0.2, utf-7;q=0.5`
   * an array sorted by quality is returned:
   * <p/>
   * ['utf-8', 'utf-7', 'iso-8859-1']
   *
   * @param charset
   */
  String acceptsCharsets(@NotNull String... charset);

  /**
   * Return accepted languages or best fit based on `langs`.
   * <p/>
   * Given `Accept-Language: en;q=0.8, es, pt`
   * an array sorted by quality is returned:
   * <p/>
   * ['es', 'pt', 'en']
   *
   * @param lang
   * @return {Array|String}
   */
  String acceptsLanguages(@NotNull String... lang);

  /**
   * Check if the incoming getRequest contains the "Content-Type"
   * get field, and it contains the give mime `type`.
   * If there is no getRequest body, `false` is returned.
   * If there is no content type, `false` is returned.
   * Otherwise, it returns true if the `type` that matches.
   * <p/>
   * Examples:
   * <p/>
   * // With Content-Type: text/html; getCharset=utf-8
   * this.is('html'); // => true
   * this.is('text/html'); // => true
   * <p/>
   * // When Content-Type is application/json
   * this.is('application/json'); // => true
   * this.is('html'); // => false
   *
   * @param type content type
   * @return The most close value
   */
  default boolean is(@NotNull String type) {
    String ct = getHeader("Content-Type");
    if (ct == null) {
      return false;
    }
    // get the content type only (exclude getCharset)
    ct = ct.split(";")[0];

    // if we received an incomplete CT
    if (type.indexOf('/') == -1) {
      // when the content is incomplete we assume */type, e.g.:
      // json -> */json
      type = "*/" + type;
    }

    // process wildcards
    if (type.contains("*")) {
      String[] parts = type.split("/");
      String[] ctParts = ct.split("/");
      return "*".equals(parts[0]) && parts[1].equals(ctParts[1]) || "*".equals(parts[1]) && parts[0].equals(ctParts[0]);

    }

    return ct.contains(type);
  }

  /**
   * Return the getRequest mime type void of
   * parameters such as "getCharset".
   *
   * @return {String}
   */
  default String getType() {
    final String type = getHeader("Content-Type");
    if (type == null) {
      return null;
    }

    return type.split(";")[0];
  }

  void setMaxLength(long limit);

  default boolean isKeepAlive() {
    final String connection = getHeader(Headers.CONNECTION);
    if (connection != null && connection.equalsIgnoreCase("close")) {
      return false;
    }

    // default should be KeepAlive
    if (getVersion().equalsIgnoreCase("HTTP/1.1")) {
      return connection != null && !connection.equalsIgnoreCase("close");
    } else {
      return connection != null && connection.equalsIgnoreCase("keep-alive");
    }
  }
}
