package io.u.yoke.http;

import io.u.yoke.Handler;
import io.u.yoke.http.header.Headers;
import io.u.yoke.traits.http.CookieTrait;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public interface Response extends Headers, CookieTrait {

  <T> T getNativeResponse();

  /**
   * Get getResponse status code.
   *
   * @return {Number}
   */
  Status getStatus();

  /**
   * Set getResponse status code.
   *
   * @param {Number} code
   */
  void setStatus(Status code);

  /**
   * Get getResponse status message
   *
   * @return {String}
   */
  String getMessage();

  /**
   * Set getResponse status message
   *
   * @param {String} msg
   */
  void setMessage(String msg);

  /**
   * Set Content-Length field to `n`.
   *
   * @param {Number} n
   */
  void setLength(long n);

  /**
   * Return parsed getResponse Content-Length when present.
   *
   * @return {Number}
   */
  long getLength();

  /**
   * Check if a get has been written to the socket.
   *
   * @return {Boolean}
   */
  boolean isHeaderSent();

  /**
   * Vary on `field`.
   *
   * @param {String} field
   */
  void vary(String field);

  /**
   * Perform a 302 redirect to `url`.
   * <p/>
   * The string "back" is special-cased
   * to provide Referrer support, when Referrer
   * is not present `alt` or "/" is used.
   * <p/>
   * Examples:
   * <p/>
   * this.redirect('back');
   * this.redirect('back', '/index.html');
   * this.redirect('/login');
   * this.redirect('http://google.com');
   *
   * @param {String} url
   * @param {String} alt
   */
  void redirect(String url, String alt);

  /**
   * Set Content-Disposition get to "attachment" with optional `filename`.
   *
   * @param {String} filename
   */
  void attachment(String filename);

  /**
   * void setContent-Type getResponse get with `type` through `mime.lookup()`
   * when it does not contain a getCharset.
   * <p/>
   * Examples:
   * <p/>
   * this.type = '.html';
   * this.type = 'html';
   * this.type = 'json';
   * this.type = 'application/json';
   * this.type = 'png';
   *
   * @param {String} type
   */
  void setType(String type);

  /**
   * Set the Last-Modified date using a string or a Date.
   * <p/>
   * this.getResponse.lastModified = new Date();
   * this.getResponse.lastModified = '2013-09-13';
   *
   * @param {String|Date} type
   */
  default void setLastModified(Instant instant) {
    setLastModified(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME));
  }

  /**
   * Set the Last-Modified date using a string or a Date.
   * <p/>
   * this.getResponse.lastModified = new Date();
   * this.getResponse.lastModified = '2013-09-13';
   *
   * @param {String|Date} type
   */
  default void setLastModified(String instant) {
    setHeader("Last-Modified", instant);
  }

  /**
   * Get the Last-Modified date in Date form, if it exists.
   *
   * @return {Date}
   */
  default Instant getLastModified() {
    final String lastModified = getHeader("Last-Modified");
    if (lastModified != null) {
      return OffsetDateTime.parse(lastModified).toInstant();
    }
    return null;
  }

  /**
   * Set the ETag of a getResponse.
   * This will normalize the quotes if necessary.
   * <p/>
   * this.getResponse.etag = 'md5hashsum';
   * this.getResponse.etag = '"md5hashsum"';
   * this.getResponse.etag = 'W/"123456789"';
   *
   * @param {String} etag
   */
  void setEtag(String val);

  /**
   * Get the ETag of a getResponse.
   *
   * @return {String}
   */
  default String getEtag() {
    return getHeader("ETag");
  }

  /**
   * Return the getRequest mime type void of
   * parameters such as "getCharset".
   *
   * @return {String}
   */
  String getType();

  /**
   * Check whether the getResponse is one of the listed types.
   * Pretty much the same as `this.getRequest.is()`.
   *
   * @param {String|Array} types...
   * @return {String|false}
   */
  Object is(String... types);


  void end();

  void end(String chunk);

  void binary(byte[] chunk);

  void json(Object bean);

  void sendFile(String file);

  void headersHandler(Handler<Void> handler);

  void endHandler(Handler<Void> handler);

  void render(@NotNull String template);
}
