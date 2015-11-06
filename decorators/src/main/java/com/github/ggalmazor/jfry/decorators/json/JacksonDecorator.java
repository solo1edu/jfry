package com.github.ggalmazor.jfry.decorators.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;
import com.github.ggalmazor.jfry.Handler;
import com.github.ggalmazor.jfry.decorators.RequestDecorator;
import com.github.ggalmazor.jfry.decorators.ResponseDecorator;

public class JacksonDecorator {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static <T> RequestDecorator deserialize() {
    return request -> request.<String, T>mapBody(JacksonDecorator::_deserialize)
        .map(request::withBody)
        .orElse(request);
  }

  public static ResponseDecorator serialize() {
    return response -> response.mapBody(JacksonDecorator::_serialize)
        .map(response::withBody)
        .orElse(response);
  }

  private static <T> T _deserialize(String json) {
    return Try.<T>of(() -> OBJECT_MAPPER.readValue(json, new POJOType<T>())).get();
  }

  private static String _serialize(Object obj) {
    return Try.of(() -> OBJECT_MAPPER.writeValueAsString(obj)).get();
  }

  public static Handler wrap(Handler handler) {
    return JacksonDecorator.deserialize().andThen(handler).andThen(JacksonDecorator.serialize())::apply;
  }


  private static class POJOType<T> extends TypeReference<T> {
  }
}
