/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.header.Headers;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * # Favicon
 * <p>
 * By default serves the Yoke favicon, or the favicon located by the given ```path```.
 */
public class Favicon implements Handler<Context> {

  /**
   * favicon cache
   */
  private final byte[] bytes;
  private final String etag;

  /**
   * Cache control for the resource
   */
  private final long maxAgeSeconds;

  /**
   * Create a new Favicon instance using a file in the file system and customizable cache period
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon("/icons/icon.ico", 1000));
   * </pre>
   *
   * @param path
   * @param maxAge
   */
  public Favicon(final String path, final long maxAge) {
    this.maxAgeSeconds = maxAge / 1000;

    try {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path == null ? "io/u/yoke/handler/favicon.ico" : null)) {
          int read;
          byte[] data = new byte[4096];
          while ((read = in.read(data, 0, data.length)) != -1) {
            out.write(data, 0, read);
          }
        }

        bytes = out.toByteArray();
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    String b64etag = null;

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      b64etag = Base64.getEncoder().encodeToString(md.digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      // ignore
    }

    etag = "\"" + (b64etag != null ? b64etag : "") + "\"";
  }

  /**
   * Create a new Favicon instance using a file in the file system and cache for 1 day.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon("/icons/icon.ico"));
   * </pre>
   *
   * @param path
   */
  public Favicon(String path) {
    this(path, 86400000);
  }

  /**
   * Create a new Favicon instance using a the default icon and cache for 1 day.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon());
   * </pre>
   */
  public Favicon() {
    this(null);
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    if ("/favicon.ico".equals(ctx.getPath())) {
      ctx.set(Headers.CONTENT_TYPE, "image/x-icon");
      ctx.set(Headers.CONTENT_LENGTH, Integer.toString(bytes.length));
      ctx.setEtag(etag);
      ctx.set(Headers.CACHE_CONTROL, "public, max-age=" + maxAgeSeconds);
      ctx.binary(bytes);
    } else {
      ctx.next();
    }
  }
}
