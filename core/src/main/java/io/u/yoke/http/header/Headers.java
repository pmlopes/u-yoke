package io.u.yoke.http.header;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Common operations on HTTP Headers
 */
public interface Headers {

  /**
   * {@code "Accept"}
   */
  String ACCEPT = "Accept";
  /**
   * {@code "Accept-Charset"}
   */
  String ACCEPT_CHARSET = "Accept-Charset";
  /**
   * {@code "Accept-Encoding"}
   */
  String ACCEPT_ENCODING = "Accept-Encoding";
  /**
   * {@code "Accept-Language"}
   */
  String ACCEPT_LANGUAGE = "Accept-Language";
  /**
   * {@code "Accept-Ranges"}
   */
  String ACCEPT_RANGES = "Accept-Ranges";
  /**
   * {@code "Accept-Patch"}
   */
  String ACCEPT_PATCH = "Accept-Patch";
  /**
   * {@code "Access-Control-Allow-Credentials"}
   */
  String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  /**
   * {@code "Access-Control-Allow-Headers"}
   */
  String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  /**
   * {@code "Access-Control-Allow-Methods"}
   */
  String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
  /**
   * {@code "Access-Control-Allow-Origin"}
   */
  String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  /**
   * {@code "Access-Control-Expose-Headers"}
   */
  String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
  /**
   * {@code "Access-Control-Max-Age"}
   */
  String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
  /**
   * {@code "Access-Control-Request-Headers"}
   */
  String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
  /**
   * {@code "Access-Control-Request-Method"}
   */
  String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
  /**
   * {@code "Age"}
   */
  String AGE = "Age";
  /**
   * {@code "Allow"}
   */
  String ALLOW = "Allow";
  /**
   * {@code "Authorization"}
   */
  String AUTHORIZATION = "Authorization";
  /**
   * {@code "Cache-Control"}
   */
  String CACHE_CONTROL = "Cache-Control";
  /**
   * {@code "Connection"}
   */
  String CONNECTION = "Connection";
  /**
   * {@code "Content-Base"}
   */
  String CONTENT_BASE = "Content-Base";
  /**
   * {@code "Content-Encoding"}
   */
  String CONTENT_ENCODING = "Content-Encoding";
  /**
   * {@code "Content-Language"}
   */
  String CONTENT_LANGUAGE = "Content-Language";
  /**
   * {@code "Content-Length"}
   */
  String CONTENT_LENGTH = "Content-Length";
  /**
   * {@code "Content-Location"}
   */
  String CONTENT_LOCATION = "Content-Location";
  /**
   * {@code "Content-Transfer-Encoding"}
   */
  String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  /**
   * {@code "Content-MD5"}
   */
  String CONTENT_MD5 = "Content-MD5";
  /**
   * {@code "Content-Range"}
   */
  String CONTENT_RANGE = "Content-Range";
  /**
   * {@code "Content-Type"}
   */
  String CONTENT_TYPE = "Content-Type";
  /**
   * {@code "Cookie"}
   */
  String COOKIE = "Cookie";
  /**
   * {@code "Date"}
   */
  String DATE = "Date";
  /**
   * {@code "ETag"}
   */
  String ETAG = "ETag";
  /**
   * {@code "Expect"}
   */
  String EXPECT = "Expect";
  /**
   * {@code "Expires"}
   */
  String EXPIRES = "Expires";
  /**
   * {@code "From"}
   */
  String FROM = "From";
  /**
   * {@code "Host"}
   */
  String HOST = "Host";
  /**
   * {@code "If-Match"}
   */
  String IF_MATCH = "If-Match";
  /**
   * {@code "If-Modified-Since"}
   */
  String IF_MODIFIED_SINCE = "If-Modified-Since";
  /**
   * {@code "If-None-Match"}
   */
  String IF_NONE_MATCH = "If-None-Match";
  /**
   * {@code "If-Range"}
   */
  String IF_RANGE = "If-Range";
  /**
   * {@code "If-Unmodified-Since"}
   */
  String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
  /**
   * {@code "Last-Modified"}
   */
  String LAST_MODIFIED = "Last-Modified";
  /**
   * {@code "Location"}
   */
  String LOCATION = "Location";
  /**
   * {@code "Max-Forwards"}
   */
  String MAX_FORWARDS = "Max-Forwards";
  /**
   * {@code "Origin"}
   */
  String ORIGIN = "Origin";
  /**
   * {@code "Pragma"}
   */
  String PRAGMA = "Pragma";
  /**
   * {@code "Proxy-Authenticate"}
   */
  String PROXY_AUTHENTICATE = "Proxy-Authenticate";
  /**
   * {@code "Proxy-Authorization"}
   */
  String PROXY_AUTHORIZATION = "Proxy-Authorization";
  /**
   * {@code "Range"}
   */
  String RANGE = "Range";
  /**
   * {@code "Referer"}
   */
  String REFERER = "Referer";
  /**
   * {@code "Retry-After"}
   */
  String RETRY_AFTER = "Retry-After";
  /**
   * {@code "Sec-WebSocket-Key1"}
   */
  String SEC_WEBSOCKET_KEY1 = "Sec-WebSocket-Key1";
  /**
   * {@code "Sec-WebSocket-Key2"}
   */
  String SEC_WEBSOCKET_KEY2 = "Sec-WebSocket-Key2";
  /**
   * {@code "Sec-WebSocket-Location"}
   */
  String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
  /**
   * {@code "Sec-WebSocket-Origin"}
   */
  String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
  /**
   * {@code "Sec-WebSocket-Protocol"}
   */
  String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
  /**
   * {@code "Sec-WebSocket-Version"}
   */
  String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
  /**
   * {@code "Sec-WebSocket-Key"}
   */
  String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
  /**
   * {@code "Sec-WebSocket-Accept"}
   */
  String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
  /**
   * {@code "Server"}
   */
  String SERVER = "Server";
  /**
   * {@code "Set-Cookie"}
   */
  String SET_COOKIE = "Set-Cookie";
  /**
   * {@code "Set-Cookie2"}
   */
  String SET_COOKIE2 = "Set-Cookie2";
  /**
   * {@code "TE"}
   */
  String TE = "TE";
  /**
   * {@code "Trailer"}
   */
  String TRAILER = "Trailer";
  /**
   * {@code "Transfer-Encoding"}
   */
  String TRANSFER_ENCODING = "Transfer-Encoding";
  /**
   * {@code "Upgrade"}
   */
  String UPGRADE = "Upgrade";
  /**
   * {@code "User-Agent"}
   */
  String USER_AGENT = "User-Agent";
  /**
   * {@code "Vary"}
   */
  String VARY = "Vary";
  /**
   * {@code "Via"}
   */
  String VIA = "Via";
  /**
   * {@code "Warning"}
   */
  String WARNING = "Warning";
  /**
   * {@code "WebSocket-Location"}
   */
  String WEBSOCKET_LOCATION = "WebSocket-Location";
  /**
   * {@code "WebSocket-Origin"}
   */
  String WEBSOCKET_ORIGIN = "WebSocket-Origin";
  /**
   * {@code "WebSocket-Protocol"}
   */
  String WEBSOCKET_PROTOCOL = "WebSocket-Protocol";
  /**
   * {@code "WWW-Authenticate"}
   */
  String WWW_AUTHENTICATE = "WWW-Authenticate";


  Comparator<String> ACCEPT_X_COMPARATOR = (o1, o2) -> {
    float f1 = getQuality(o1);
    float f2 = getQuality(o2);
    if (f1 < f2) {
      return 1;
    }
    if (f1 > f2) {
      return -1;
    }
    return 0;
  };

  static String getParameter(@NotNull final String headerValue, @NotNull final String parameter) {
    String[] params = headerValue.split(" *; *");
    for (int i = 1; i < params.length; i++) {
      String[] parameters = params[1].split(" *= *");
      if (parameter.equals(parameters[0])) {
        return parameters[1];
      }
    }
    return null;
  }

  static float getQuality(final String headerValue) {
    if (headerValue == null) {
      return 0;
    }

    final String q = getParameter(headerValue, "q");

    if (q == null) {
      return 1;
    }

    return Float.parseFloat(q);
  }

  /**
   * Returns all get names for this request.
   */
  Iterable<String> getHeaders();

  /**
   * Return request get.
   * <p>
   * The `Referrer` get field is special-cased,
   * both `Referrer` and `Referer` are interchangeable.
   * <p>
   * Examples:
   * <p>
   * this.get('Content-Type');
   * // => "text/plain"
   * <p>
   * this.get('content-type');
   * // => "text/plain"
   * <p>
   * this.get('Something');
   * // => null
   *
   * @param name get name
   * @return the value for the get
   */
  String getHeader(@NotNull String name);

  /**
   * Allow getting a multi value get.
   *
   * @param name The key to get
   * @return The list of all found objects
   */
  Iterable<String> getHeaderValues(@NotNull String name);

  default Iterable<String> getSortedHeader(@NotNull final String name) {
    String accept = getHeader(name);
    // accept anything when accept is not present
    if (accept == null) {
      return Collections.emptyList();
    }

    // parse
    String[] items = accept.split(" *, *");
    // sort on quality
    Arrays.sort(items, ACCEPT_X_COMPARATOR);

    List<String> list = new ArrayList<>(items.length);

    for (String item : items) {
      // find any ; e.g.: "application/json;q=0.8"
      int space = item.indexOf(';');

      if (space != -1) {
        list.add(item.substring(0, space));
      } else {
        list.add(item);
      }
    }

    return list;
  }

  /**
   * Set get `field` to `val`, or pass
   * an object of get fields.
   * <p/>
   * Examples:
   * <p/>
   * this.putAt('Foo', ['bar', 'baz']);
   * this.putAt('Accept', 'application/json');
   * this.putAt({ Accept: 'text/plain', 'X-API-Key': 'tobi' });
   *
   * @param {String|Object|Array} field
   * @param {String}              val
   */
  void setHeader(@NotNull String name, String value);

  /**
   * Append additional header `field` with value `val`.
   * <p/>
   * Examples:
   * <p/>
   * this.append('Link', ['<http://localhost/>', '<http://localhost:3000/>']);
   * this.append('Set-Cookie', 'foo=bar; Path=/; HttpOnly');
   * this.append('Warning', '199 Miscellaneous warning');
   *
   * @param {String}       field
   * @param {String|Array} val
   * @api public
   */
  void appendHeader(@NotNull String name, String value);

  /**
   * Remove get `field`.
   *
   * @param {String} name
   */
  void removeHeader(@NotNull String name);
}
