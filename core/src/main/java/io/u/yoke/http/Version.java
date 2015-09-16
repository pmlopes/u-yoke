package io.u.yoke.http;

public enum Version {
  HTTP_1_0("http/1.0"),
  HTTP_1_1("http/1.1"),
  HTTP_2_0("h2");

  private final String version;

  Version(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return version;
  }
}
