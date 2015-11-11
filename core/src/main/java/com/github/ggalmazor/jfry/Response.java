package com.github.ggalmazor.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.control.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Response {
  private final Request request;
  private final Status status;
  private final Option<Object> body;
  private final Map<String, String> headers;

  private Response(Request request, Status status, Option<Object> body, Map<String, String> headers) {
    this.request = request;
    this.status = status;
    this.body = body;
    this.headers = headers;
  }

  public static Response from(Request request) {
    return new Response(request, Status.INTERNAL_SERVER_ERROR, Option.none(), new HashMap<>());
  }

  public Status getStatus() {
    return status;
  }

  @SuppressWarnings("unchecked")
  public Option<Object> getBody() {
    return body;
  }

  public void forEachHeader(BiConsumer<String, String> biConsumer) {
    List.ofAll(headers.entrySet()).forEach(e -> biConsumer.accept(e.getKey(), e.getValue()));
  }

  public boolean hasBody() {
    return body.isDefined();
  }

  public void ifHasBody(Consumer<Object> consumer) {
    body.forEach(consumer);
  }

  public Response withBody(Object body) {
    return new Response(request, status, Option.of(body), headers);
  }

  @SafeVarargs
  public final Response withHeaders(Tuple2<String, String>... header) {
    Map<String, String> newHeaders = new HashMap<>();
    newHeaders.putAll(this.headers);
    newHeaders.putAll(List.<Tuple2<String, String>>of(header).toJavaMap(Function.identity()));
    return new Response(request, status, body, newHeaders);
  }

  @SuppressWarnings("unchecked")
  public <T, U> Option<U> mapBody(Function<T, U> mapper) {
    return body.map(b -> (T) b).map(mapper);
  }

  public <T> T map(Function<Response, T> mapper) {
    return mapper.apply(this);
  }

  public Response ok(Object body) {
    return new Response(request, Status.OK, Option.of(body), headers);
  }

  public Response noContent() {
    return new Response(request, Status.NO_CONTENT, body, headers);
  }

  public Response badRequest() {
    return new Response(request, Status.BAD_REQUEST, body, headers);
  }

  public Response unauthorized() {
    return new Response(request, Status.UNAUTHORIZED, body, headers);
  }

  public Response notFound() {
    return new Response(request, Status.NOT_FOUND, body, headers);
  }

  public Response internalServerError() {
    return new Response(request, Status.INTERNAL_SERVER_ERROR, body, headers);
  }

  public enum Status {
    OK(200),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    public final int code;

    Status(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }
}
