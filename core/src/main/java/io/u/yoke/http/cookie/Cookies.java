package io.u.yoke.http.cookie;

import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.util.Iterator;

public interface Cookies {

  Iterable<HttpCookie> getCookies();

  default HttpCookie getCookie(@NotNull String name) {
    for (HttpCookie cookie : getCookies()) {
      if (cookie.getName().equals(name)) {
        return cookie;
      }
    }

    return null;
  }

  void addCookie(@NotNull HttpCookie cookie);

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
