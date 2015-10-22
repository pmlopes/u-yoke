package io.u.yoke.http.impl;

import java.net.HttpCookie;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class ServerCookieEncoder {

  private static final String EXPIRES = "Expires";
  private static final String PATH = "Path";
  private static final String SECURE = "Secure";
  private static final String HTTP_ONLY = "HttpOnly";
  private static final String DOMAIN = "Domain";
  private static final String VERSION = "Version";
  private static final String COMMENT = "Comment";
  private static final String COMMENT_URL = "CommentUrl";
  private static final String MAX_AGE = "MaxAge";
  private static final String PORT = "Port";
  private static final String DISCARD = "Discard";

  private static final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

  private ServerCookieEncoder() {
  }

  public static String encode(HttpCookie cookie) {
    final StringBuilder sb = new StringBuilder();

    add(sb, cookie.getName(), cookie.getValue());

    if (cookie.getDomain() != null) {
      if (cookie.getVersion() > 0) {
        add(sb, DOMAIN, cookie.getDomain());
      } else {
        addUnquoted(sb, DOMAIN, cookie.getDomain());
      }
    }

    if (cookie.getPath() != null) {
      if (cookie.getVersion() > 0) {
        add(sb, PATH, cookie.getPath());
      } else {
        addUnquoted(sb, PATH, cookie.getPath());
      }
    }

    if (cookie.getMaxAge() != -1) {
      if (cookie.getVersion() == 0) {
        // special treatment, meaning: expire now!
        if (cookie.getMaxAge() == 0) {
          addUnquoted(sb, EXPIRES, "Thu, 01 Jan 1970 00:00:00 GMT");
        } else {
          OffsetDateTime offsetDateTime = OffsetDateTime.now().plus(cookie.getMaxAge(), ChronoUnit.SECONDS);
          addUnquoted(sb, EXPIRES, formatter.format(offsetDateTime));
        }
      } else {
        add(sb, MAX_AGE, cookie.getMaxAge());
      }
    }

    if (cookie.getSecure()) {
      sb.append(SECURE);
      sb.append(';');
      sb.append(' ');
    }
    if (cookie.isHttpOnly()) {
      sb.append(HTTP_ONLY);
      sb.append(';');
      sb.append(' ');
    }
    if (cookie.getVersion() >= 1) {
      if (cookie.getComment() != null) {
        add(sb, COMMENT, cookie.getComment());
      }

      add(sb, VERSION, 1);

      if (cookie.getCommentURL() != null) {
        addQuoted(sb, COMMENT_URL, cookie.getCommentURL());
      }

      if (cookie.getPortlist() != null) {
        sb.append(PORT);
        sb.append('=');
        sb.append('\"');
        sb.append(cookie.getPortlist());
        sb.append('\"');
        sb.append(';');
        sb.append(' ');
      }
      if (cookie.getDiscard()) {
        sb.append(DISCARD);
        sb.append(';');
        sb.append(' ');
      }
    }

    if (sb.length() == 0) {
      return null;
    }

    if (sb.length() > 0) {
      sb.setLength(sb.length() - 2);
    }

    return sb.toString();
  }

  private static void add(StringBuilder sb, String name, long val) {
    add(sb, name, Long.toString(val));
  }

  private static void add(StringBuilder sb, String name, int val) {
    add(sb, name, Integer.toString(val));
  }

  private static void add(StringBuilder sb, String name, String val) {
    if (val == null) {
      addQuoted(sb, name, "");
      return;
    }

    for (int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);
      switch (c) {
        case '\t':
        case ' ':
        case '"':
        case '(':
        case ')':
        case ',':
        case '/':
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case '[':
        case '\\':
        case ']':
        case '{':
        case '}':
          addQuoted(sb, name, val);
          return;
      }
    }

    addUnquoted(sb, name, val);
  }

  private static void addUnquoted(StringBuilder sb, String name, String val) {
    sb.append(name);
    sb.append('=');
    sb.append(val);
    sb.append(';');
    sb.append(' ');
  }

  private static void addQuoted(StringBuilder sb, String name, String val) {
    if (val == null) {
      val = "";
    }

    sb.append(name);
    sb.append('=');
    sb.append('\"');
    sb.append(val.replace("\\", "\\\\").replace("\"", "\\\""));
    sb.append('\"');
    sb.append(';');
    sb.append(' ');
  }
}
