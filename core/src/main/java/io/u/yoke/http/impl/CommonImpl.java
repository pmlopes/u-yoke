package io.u.yoke.http.impl;

import io.u.yoke.http.cookie.Cookies;
import io.u.yoke.http.header.Headers;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.*;

abstract class CommonImpl implements Headers, Cookies {

  private final Headers headers;

  private List<HttpCookie> cookies;

  public CommonImpl(Headers headers) {
    this.headers = headers;
  }

  @Override
  public Iterable<String> getHeaders() {
    return headers.getHeaders();
  }

  @Override
  public String getHeader(@NotNull String name) {
    if ("referer".equalsIgnoreCase(name) || "referrer".equalsIgnoreCase(name)) {
      final String header = headers.getHeader("referrer");
      return (header != null) ? header : headers.getHeader("referer");
    }

    return headers.getHeader(name);
  }

  @Override
  public Iterable<String> getHeaderValues(@NotNull String name) {
    if ("referer".equalsIgnoreCase(name) || "referrer".equalsIgnoreCase(name)) {
      final Iterable<String> header = headers.getHeaderValues("referrer");
      return (header != null) ? header : headers.getHeaderValues("referer");
    }

    return headers.getHeaderValues(name);
  }

  @Override
  public void setHeader(@NotNull String name, String value) {
    headers.setHeader(name, value);
  }

  @Override
  public void appendHeader(@NotNull String name, String value) {
    headers.appendHeader(name, value);
  }

  @Override
  public void removeHeader(@NotNull String name) {
    headers.removeHeader(name);
  }

  @Override
  public Iterable<HttpCookie> getCookies() {
    if (cookies == null) {
      final String cookieHeader = getHeader("Cookie");
      if (cookieHeader == null) {
        cookies = new LinkedList<>();
      } else {
        cookies = HttpCookie.parse(cookieHeader);
      }
    }
    return cookies;
  }

  @Override
  public void addCookie(@NotNull HttpCookie cookie) {
    cookies.add(cookie);
  }
}
