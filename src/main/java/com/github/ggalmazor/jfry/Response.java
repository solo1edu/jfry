package com.github.ggalmazor.jfry;

import javaslang.Tuple;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Option;

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
    return new Response(request, Status.INTERNAL_SERVER_ERROR, Option.none(), HashMap.empty());
  }

  public Status getStatus() {
    return status;
  }

  public Option<Object> getBody() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void forEachHeader(BiConsumer<String, String> biConsumer) {
    headers.forEach(t -> biConsumer.accept(t._1, t._2));
  }

  public boolean hasBody() {
    return body.isDefined();
  }

  public void forEachBody(Consumer<Object> consumer) {
    body.forEach(consumer);
  }

  public Response withBody(Object body) {
    return new Response(request, status, Option.of(body), headers);
  }

  public Response withHeader(String name, String value) {
    return new Response(request, status, body, headers.put(name, value));
  }

  public Response withHeaders(String... nameAndValues) {
    // TODO Find out why first compilation fails with HashMap.<String,String>of(nameAndValues);
    Map<String, String> headers = List.of(nameAndValues).sliding(2).toMap(p -> Tuple.of(p.get(0), p.get(1)));
    return new Response(request, status, body, headers.merge(this.headers));
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

  public <T> Option<T> mapHeader(String name, Function<String, T> mapper) {
    return headers.get(name).map(mapper);
  }

  public Option<String> getHeader(String name) {
    return headers.get(name);
  }

  public enum Status {
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    public final int code;
    public final String msg;

    Status(int code, String msg) {
      this.code = code;
      this.msg = msg;
    }

    public int getCode() {
      return code;
    }

    public String getMsg() {
      return msg;
    }
  }
}
