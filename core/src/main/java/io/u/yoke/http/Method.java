package io.u.yoke.http;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface Method {

  // standard HTTP methods
  Method GET = new MethodImpl("GET");
  Method POST = new MethodImpl("POST");
  Method PUT = new MethodImpl("PUT");
  Method PATCH = new MethodImpl("PATCH");
  Method DELETE = new MethodImpl("DELETE");
  Method TRACE = new MethodImpl("TRACE");
  Method CONNECT = new MethodImpl("CONNECT");
  Method OPTIONS = new MethodImpl("OPTIONS");
  Method HEAD = new MethodImpl("HEAD");

  String name();

  static Method valueOf(@NotNull String name) {
    switch (name) {
      case "GET":
        return GET;
      case "POST":
        return POST;
      case "PUT":
        return PUT;
      case "PATCH":
        return PATCH;
      case "DELETE":
        return DELETE;
      case "TRACE":
        return TRACE;
      case "CONNECT":
        return CONNECT;
      case "OPTIONS":
        return OPTIONS;
      case "HEAD":
        return HEAD;
      default:
        return new MethodImpl(name);
    }
  }

  static Iterable<Method> values() {
    return Arrays.asList(GET, POST, PUT, PATCH, DELETE, TRACE, CONNECT, OPTIONS, HEAD);
  }
}

class MethodImpl implements Method {

  private final String name;

  MethodImpl(@NotNull String method) {
    this.name = method.toUpperCase();
  }

  @Override
  public String name() {
    return name;
  }
}
