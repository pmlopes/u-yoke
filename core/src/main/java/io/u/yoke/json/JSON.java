package io.u.yoke.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.u.yoke.starter.JSMain;

import java.io.IOException;

public class JSON {

  private JSON () {}

  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.registerModule(new AfterburnerModule());
  }

  public static String encode(Object bean) throws JsonProcessingException {
    return MAPPER.writeValueAsString(bean);
  }

  public static byte[] encodeToBytes(Object bean) throws JsonProcessingException {
    return MAPPER.writeValueAsBytes(bean);
  }

  public static <T> T decode(String value, Class<T> clazz) throws IOException {
    return MAPPER.readValue(value, clazz);
  }

  public static <T> T decode(byte[] value, Class<T> clazz) throws IOException {
    return MAPPER.readValue(value, clazz);
  }
}
