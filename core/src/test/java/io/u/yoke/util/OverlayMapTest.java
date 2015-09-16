package io.u.yoke.util;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class OverlayMapTest {

  @Test
  public void testOverlayMap118_1() {
    Map<String, Object> ro = new HashMap<String, Object>() {{
      put("title", "ro-title");
    }};

    OverlayMap ctx = new OverlayMap(ro);
    ctx.put("title", "rw-title");

    Set<String> keys = ctx.keySet();
    Set<String> expectedKeys = new HashSet<String>() {{
      add("title");
    }};

    assertEquals(expectedKeys, keys);
  }

  @Test
  public void testOverlayMap118_2() {
    Map<String, Object> ro = new HashMap<String, Object>() {{
      put("title", "ro-title");
    }};

    OverlayMap ctx = new OverlayMap(ro);
    ctx.put("title", "rw-title");

    Collection<Object> values = ctx.values();
    Collection<Object> expectedValues = new LinkedList<Object>() {{
      add("rw-title");
    }};

    assertEquals(expectedValues, values);
  }

  @Test
  public void testOverlayMap118_3() {
    Map<String, Object> ro = new HashMap<String, Object>() {{
      put("title", "ro-title");
    }};

    OverlayMap ctx = new OverlayMap(ro);
    ctx.put("title", "rw-title");

    Set<Map.Entry<String, Object>> entrySet = ctx.entrySet();
    assertEquals(1, entrySet.size());
  }
}
