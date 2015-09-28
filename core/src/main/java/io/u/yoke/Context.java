package io.u.yoke;

import io.u.yoke.http.Method;
import io.u.yoke.http.Request;
import io.u.yoke.http.Response;
import io.u.yoke.http.Status;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.time.Instant;

public interface Context {

  Request getRequest();

  Response getResponse();

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
    getResponse().attachment(filename);
  }

  default void redirect(String url, String alt) {
    getResponse().redirect(url, alt);
  }

  default void remove(String field) {
    getResponse().removeHeader(field);
  }

  default void vary(String field) {
    getResponse().vary(field);
  }

  default void set(String field, String val) {
    getResponse().setHeader(field, val);
  }

  default void append(String field, String val) {
    getResponse().appendHeader(field, val);
  }

  default void setStatus(Status code) {
    getResponse().setStatus(code);
  }

  default void setMessage(String msg) {
    getResponse().setMessage(msg);
  }

  default void end(String chunk) {
    getResponse().end(chunk);
  }

  default void binary(byte[] chunk) {
    getResponse().binary(chunk);
  }

  default void end() {
    getResponse().end();
  }

  default void json(Object bean) {
    getResponse().json(bean);
  }

  default void setLength(long n) {
    getResponse().setLength(n);
  }

  default void setType(String type) {
    getResponse().setType(type);
  }

  default void setLastModified(Instant val) {
    getResponse().setLastModified(val);
  }

  default void setLastModified(String val) {
    getResponse().setLastModified(val);
  }

  default void setEtag(String val) {
    getResponse().setEtag(val);
  }

  default boolean isHeaderSent() {
    return getResponse().isHeaderSent();
  }

  default void addCookie(HttpCookie cookie) {
    getResponse().addCookie(cookie);
  }

  default void removeCookie(String name) {
    getResponse().removeCookie(name);
  }

  /**
   * Request delegation.
   */

  default String acceptsLanguages(@NotNull String... lang) {
    return getRequest().acceptsLanguages(lang);
  }

  default String acceptsEncodings(@NotNull String... encoding) {
    return getRequest().acceptsEncodings(encoding);
  }

  default String acceptsCharsets(@NotNull String... charset) {
    return getRequest().acceptsCharsets(charset);
  }

  default String accepts(@NotNull String... type) {
    return getRequest().accepts(type);
  }

  default String get(@NotNull String name) {
    return getRequest().getHeader(name);
  }

  default boolean is(@NotNull String type) {
    return getRequest().is(type);
  }

  default void setMethod(@NotNull Method method) {
    getRequest().setMethod(method);
  }

  default void setPath(String path) {
    getRequest().setPath(path);
  }

  default void setQuery(String obj) {
    getRequest().setQuery(obj);
  }

  default void setURI(String val) {
    getRequest().setURI(val);
  }

  default boolean isIdempotent() {
    return getRequest().isIdempotent();
  }

  default Iterable<String> getSubdomains() {
    return getRequest().getSubdomains();
  }

  default String getProtocol() {
    return getRequest().getProtocol();
  }

  default String getPath() {
    return getRequest().getPath();
  }

  default String getHost() {
    return getRequest().getHost();
  }

  default String getHostname() {
    return getRequest().getHostname();
  }

  default Iterable<String> getHeaders() {
    return getRequest().getHeaders();
  }

  default boolean isSecure() {
    return getRequest().isSecure();
  }

  default boolean isStale() {
    return getRequest().isStale();
  }

  default boolean isFresh() {
    return getRequest().isFresh();
  }

  default Iterable<String> getIps() {
    return getRequest().getIps();
  }

  default String getIp() {
    return getRequest().getIp();
  }

  default Iterable<HttpCookie> getCookies() {
    return getRequest().getCookies();
  }

  default HttpCookie getCookie(@NotNull String name) {
    return getRequest().getCookie(name);
  }
}
