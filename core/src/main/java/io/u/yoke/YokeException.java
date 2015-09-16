package io.u.yoke;

import io.u.yoke.http.Status;

public class YokeException extends RuntimeException {

  private final Status status;

  public YokeException(Status status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public YokeException(String message, Throwable cause) {
    this(Status.INTERNAL_SERVER_ERROR, message, cause);
  }

  public YokeException(String message) {
    super(message);
    this.status = Status.INTERNAL_SERVER_ERROR;
  }

  public YokeException(Status status) {
    super(status.getDescription());
    this.status = status;
  }

  public YokeException(Status status, String message) {
    super(message);
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
