package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.time.Instant;

public interface Context {

  Request request();

  Response response();

  /**
   * Allow getting properties in a generified way.
   *
   * @param name The key to get
   * @return {R} The found object
   */
  <R> R getAt(@NotNull final String name);

  /**
   * Allow getting properties in a generified way and return defaultValue if the key does not exist.
   *
   * @param name         The key to get
   * @param defaultValue value returned when the key does not exist
   * @return {R} The found object
   */
  <R> R getAt(@NotNull final String name, R defaultValue);

  /**
   * Allows putting a value into the context
   *
   * @param name  the key to store
   * @param value the value to store
   * @return {R} the previous value or null
   */
  <R> R putAt(@NotNull final String name, R value);

  default void fail(Status statusCode) {
    fail(statusCode, statusCode.getDescription());
  }

  default void fail(String errorMessage) {
    fail(Status.INTERNAL_SERVER_ERROR, errorMessage);
  }

  default void fail(Throwable throwable) {
    fail(Status.INTERNAL_SERVER_ERROR, throwable);
  }

  void fail(Status status, String message);

  default void fail(Status statusCode, Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace(System.err);
      fail(statusCode, throwable.getMessage(), throwable);
    }
  }

  void fail(Status status, String message, Throwable cause);

  void next();

  /**
   * Response delegation.
   */

  default void attachment(String filename) {
    response().attachment(filename);
  }

  default void redirect(String url, String alt) {
    response().redirect(url, alt);
  }

  default void remove(String field) {
    response().removeHeader(field);
  }

  default void vary(String field) {
    response().vary(field);
  }

  default void set(String field, String val) {
    response().setHeader(field, val);
  }

  default void append(String field, String val) {
    response().appendHeader(field, val);
  }

  default void setStatus(Status code) {
    response().setStatus(code);
  }

  default void setMessage(String msg) {
    response().setMessage(msg);
  }

  default void end(String chunk) {
    response().end(chunk);
  }

  default void binary(byte[] chunk) {
    response().binary(chunk);
  }

  default void end() {
    response().end();
  }

  default void json(Object bean) {
    response().json(bean);
  }

  default void setLength(long n) {
    response().setLength(n);
  }

  default void setType(String type) {
    response().setType(type);
  }

  default void setLastModified(Instant val) {
    response().setLastModified(val);
  }

  default void setLastModified(String val) {
    response().setLastModified(val);
  }

  default void setEtag(String val) {
    response().setEtag(val);
  }

  default boolean isHeaderSent() {
    return response().isHeaderSent();
  }

  default void addCookie(HttpCookie cookie) {
    response().addCookie(cookie);
  }

  default void removeCookie(String name) {
    response().removeCookie(name);
  }

  /**
   * Request delegation.
   */

  default String acceptsLanguages(@NotNull String... lang) {
    return request().acceptsLanguages(lang);
  }

  default String acceptsEncodings(@NotNull String... encoding) {
    return request().acceptsEncodings(encoding);
  }

  default String acceptsCharsets(@NotNull String... charset) {
    return request().acceptsCharsets(charset);
  }

  default String accepts(@NotNull String... type) {
    return request().accepts(type);
  }

  default String get(@NotNull String name) {
    return request().getHeader(name);
  }

  default boolean is(@NotNull String type) {
    return request().is(type);
  }

  default void setMethod(@NotNull Method method) {
    request().setMethod(method);
  }

  default void setPath(String path) {
    request().setPath(path);
  }

  default void setQuery(String obj) {
    request().setQuery(obj);
  }

  default void setURI(String val) {
    request().setURI(val);
  }

  default boolean isIdempotent() {
    return request().isIdempotent();
  }

  default Iterable<String> getSubdomains() {
    return request().getSubdomains();
  }

  default String getProtocol() {
    return request().getProtocol();
  }

  default String getPath() {
    return request().getPath();
  }

  default String getHost() {
    return request().getHost();
  }

  default String getHostname() {
    return request().getHostname();
  }

  default Iterable<String> getHeaders() {
    return request().getHeaders();
  }

  default boolean isSecure() {
    return request().isSecure();
  }

  default boolean isStale() {
    return request().isStale();
  }

  default boolean isFresh() {
    return request().isFresh();
  }

  default Iterable<String> getIps() {
    return request().getIps();
  }

  default String getIp() {
    return request().getIp();
  }

  default Iterable<HttpCookie> getCookies() {
    return request().getCookies();
  }

  default HttpCookie getCookie(@NotNull String name) {
    return request().getCookie(name);
  }
}
