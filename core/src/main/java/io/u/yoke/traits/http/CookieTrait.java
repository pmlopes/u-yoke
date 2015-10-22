package io.u.yoke.traits.http;

import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.Collections;
import java.util.Iterator;

public interface CookieTrait {

  default Iterable<HttpCookie> getCookies() {
    return Collections.emptyList();
  }

  default HttpCookie getCookie(@NotNull String name) {
    for (HttpCookie cookie : getCookies()) {
      if (cookie.getName().equals(name)) {
        return cookie;
      }
    }

    return null;
  }

  default void addCookie(@NotNull HttpCookie cookie) {
    throw new UnsupportedOperationException();
  }

  default void removeCookie(@NotNull String name) {
    Iterator<HttpCookie> it = getCookies().iterator();
    while (it.hasNext()) {
      if (it.next().getName().equals(name)) {
        it.remove();
        return;
      }
    }
  }
}
