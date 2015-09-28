/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

/**
 * # Logger
 * <p>
 * Logger for getRequest. There are 3 formats included:
 * 1. DEFAULT
 * 2. SHORT
 * 3. TINY
 * <p>
 * Default tries to log in a format similar to Apache log format, while the other 2 are more suited to development mode.
 * The logging depends on Vert.x logger settings and the severity of the error, so for errors with status greater or
 * equal to 500 the fatal severity is used, for status greater or equal to 400 the error severity is used, for status
 * greater or equal to 300 warn is used and for status above 100 info is used.
 */
public class Logger implements Handler<Context> {

  private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Logger.class.getName());

  /**
   * The possible out of the box formats.
   */
  public enum Format {
    DEFAULT,
    SHORT,
    TINY
  }

  /**
   * log before getRequest or after
   */
  private final boolean immediate;

  /**
   * the current choosen format
   */
  private final Format format;

  public Logger(final boolean immediate, @NotNull Format format) {
    this.immediate = immediate;
    this.format = format;
  }

  public Logger(@NotNull Format format) {
    this(false, format);
  }

  public Logger() {
    this(false, Format.DEFAULT);
  }

  private void log(final Context ctx, final Instant instant, final String remoteClient, final String version, final String method, final String uri) {
    long contentLength = 0;
    if (immediate) {
      Object obj = ctx.get("content-length");
      if (obj != null) {
        contentLength = Long.parseLong(obj.toString());
      }
    } else {
      Object obj = ctx.getResponse().getHeader("content-length");
      if (obj != null) {
        contentLength = Long.parseLong(obj.toString());
      }
    }

    int status = ctx.getResponse().getStatus().getCode();
    String message = null;

    switch (format) {
      case DEFAULT:
        String referrer = ctx.get("referrer");
        String userAgent = ctx.get("user-agent");

        message = String.format("%s - - [%s] \"%s %s %s\" %d %d \"%s\" \"%s\"",
            remoteClient,
            OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME),
            method,
            uri,
            version,
            status,
            contentLength,
            referrer == null ? "" : referrer,
            userAgent == null ? "" : userAgent);
        break;
      case SHORT:
        message = String.format("%s - %s %s %s %d %d - %d ms",
            remoteClient,
            method,
            uri,
            version,
            status,
            contentLength,
            (System.currentTimeMillis() - instant.toEpochMilli()));
        break;
      case TINY:
        message = String.format("%s %s %d %d - %d ms",
            method,
            uri,
            status,
            contentLength,
            (System.currentTimeMillis() - instant.toEpochMilli()));
        break;
    }

    logMessage(status, message);
  }

  protected void logMessage(int status, String message) {
    if (status >= 500) {
      logger.severe(message);
    } else if (status >= 400) {
      logger.severe(message);
    } else if (status >= 300) {
      logger.warning(message);
    } else {
      logger.info(message);
    }
  }

  @Override
  public void handle(@NotNull final Context ctx) {

    // common logging data
    final Instant instant = Instant.now();
    final String remoteClient = ctx.getRequest().getIp();
    final String method = ctx.getRequest().getMethod().name();
    final String uri = ctx.getRequest().getURI();
    final String version = ctx.getRequest().getVersion().toString();

    if (immediate) {
      log(ctx, instant, remoteClient, version, method, uri);
    } else {
      ctx.getResponse().endHandler(event -> log(ctx, instant, remoteClient, version, method, uri));
    }

    ctx.next();
  }
}
