package io.u.yoke.util;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class HTTPEncode {

  private HTTPEncode() {}

  public static String encodeURIComponent(@NotNull String s) {
    String result;

    try {
      result = URLEncoder.encode(s, "UTF-8")
          .replaceAll("\\+", "%20")
          .replaceAll("%21", "!")
          .replaceAll("%27", "'")
          .replaceAll("%28", "(")
          .replaceAll("%29", ")")
          .replaceAll("%7E", "~");
    } catch (UnsupportedEncodingException e) {
      result = s;
    }

    return result;
  }

  public static String decodeURIComponent(@NotNull String s) {
    String result;

    try {
      result = URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      result = s;
    }

    return result;
  }
}
