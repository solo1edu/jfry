package org.jfry.decorators.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import org.jfry.decorators.RequestDecorator;
import org.jfry.decorators.ResponseDecorator;

public class JacksonDecorator {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static <T> RequestDecorator deserialize() {
    return request -> {
      String json = request.getBody();
      T t = Try.<T>of(() -> OBJECT_MAPPER.readValue(json, new POJOType<T>())).get();
      return request.withBody(t);
    };
  }

  public static ResponseDecorator serialize() {
    return response -> {
      String json = Try.of(() -> OBJECT_MAPPER.writeValueAsString(response.getBody())).get();
      return response.withBody(json);
    };
  }


  private static class POJOType<T> extends TypeReference<T> {
  }
}
