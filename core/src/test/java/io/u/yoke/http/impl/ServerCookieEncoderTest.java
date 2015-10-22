package io.u.yoke.http.impl;

import org.junit.Test;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ServerCookieEncoderTest {
  
  Map<String, String> cookies = new HashMap<String, String>() {{
    put("Set-Cookie: theme=light", "theme=light");
    put("Set-Cookie: sessionToken=abc123; Expires=Wed, 9 Jun 2021 10:18:14 GMT", "sessionToken=abc123; Expires=Wed, 9 Jun 2021 10:18:.. GMT");
    put("Set-Cookie: LSID=DQAAAK…Eaem_vYg; Path=/accounts; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly", "LSID=DQAAAK…Eaem_vYg; Path=/accounts; Expires=Wed, 13 Jan 2021 22:23:.. GMT; Secure; HttpOnly");
    put("Set-Cookie: HSID=AYQEVn….DKrdst; Domain=.foo.com; Path=/; Expires=Wed, 13 Jan 2021 22:23:01 GMT; HttpOnly", "HSID=AYQEVn…\\.DKrdst; Domain=\\.foo\\.com; Path=/; Expires=.+ GMT; HttpOnly");
    put("Set-Cookie: SSID=Ap4P….GTEq; Domain=foo.com; Path=/; Expires=Wed, 13 Jan 2021 22:23:01 GMT; Secure; HttpOnly", "SSID=Ap4P…\\.GTEq; Domain=foo\\.com; Path=/; Expires=Wed, 13 Jan 2021 22:23:.. GMT; Secure; HttpOnly");
    put("Set-Cookie: lu=Rg3vHJZnehYLjVg7qi3bZjzg; Domain=.example.com; Path=/; Expires=Tue, 15-Jan-2013 21:47:38 GMT; HttpOnly", "lu=Rg3vHJZnehYLjVg7qi3bZjzg; Domain=\\.example\\.com; Path=/; Expires=Tue, 15 Jan 2013 21:47:.. GMT; HttpOnly");
    put("Set-Cookie: made_write_conn=1295214458; Path=/; Domain=.example.com", "made_write_conn=1295214458; Domain=\\.example\\.com; Path=/");
    put("Set-Cookie: reg_fb_gate=deleted; Expires=Thu, 01-Jan-1970 00:00:01 GMT; Path=/; Domain=.example.com; HttpOnly", "reg_fb_gate=deleted; Domain=\\.example\\.com; Path=/; Expires=Thu, 1 Jan 1970 00:00:.. GMT; HttpOnly");
  }};

  @Test
  public void test() {

    for (Map.Entry<String, String> header : cookies.entrySet()) {
      assertTrue(ServerCookieEncoder.encode(HttpCookie.parse(header.getKey()).get(0)).matches(header.getValue()));
    }
  }
}