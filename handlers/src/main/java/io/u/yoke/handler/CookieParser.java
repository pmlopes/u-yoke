/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.Status;
import io.u.yoke.security.Security;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import java.net.HttpCookie;

/**
 * # CookieParser
 * <p>
 * Parse request cookie both signed or plain.
 * <p>
 * If a cooke value starts with *s:* it means that it is a signed cookie. In this case the value is expected to be
 * *s:&lt;cookie&gt;.&lt;signature&gt;*. The signature is *HMAC + SHA256*.
 * <p>
 * When the Cookie parser is initialized with a secret then that value is used to verify if a cookie is valid.
 */
public class CookieParser implements Handler<Context> {

  /**
   * Message Signer
   */
  private final Mac mac;

  /**
   * Instantiates a CookieParser with a given Mac.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new CookieParser(YokeSecurity.newHmacSHA256("s3cr3t")));
   * </pre>
   *
   * @param mac Mac
   */
  public CookieParser(@NotNull final Mac mac) {
    this.mac = mac;
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    String cookieHeader = ctx.get("Cookie");

    if (cookieHeader != null) {
      for (HttpCookie cookie : ctx.request().getCookies()) {
        String value = cookie.getValue();

        // if the prefix is there then it is signed
        if (value.startsWith("s:")) {
          value = Security.unsign(value.substring(2), mac);
          // value cannot be null in a cookie if the signature is mismatch then this value will be null
          // in that case the cookie has been tampered
          if (value == null) {
            ctx.fail(Status.BAD_REQUEST);
            return;
          }
          cookie.setValue(value);
        }
      }
    }

    // install a engine to sign response cookie
    ctx.response().headersHandler(v -> {
      for (HttpCookie cookie : ctx.response().getCookies()) {
        cookie.setValue("s:" + Security.sign(cookie.getValue(), mac));
      }
    });

    ctx.next();
  }
}
