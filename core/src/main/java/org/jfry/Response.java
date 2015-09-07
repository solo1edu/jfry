package org.jfry;

import javaslang.control.Option;
import javaslang.unsafe;

import java.util.function.Consumer;
import java.util.function.Function;

public class Response {
  private final Request request;
  private final Status status;
  private final Option<Object> body;

  private Response(Request request, Status status, Option<Object> body) {
    this.request = request;
    this.status = status;
    this.body = body;
  }

  public static Response from(Request request) {
    return new Response(request, Status.INTERNAL_SERVER_ERROR, Option.none());
  }

  public Status getStatus() {
    return status;
  }

  @unsafe
  @SuppressWarnings("unchecked")
  public <T> T getBody() {
    return (T) body.get();
  }

  public boolean hasBody() {
    return body.isDefined();
  }

  public <T> void ifHasBody(Consumer<T> consumer) {
    consumer.accept(getBody());
  }

  public Response withBody(Object body) {
    return new Response(request, status, Option.of(body));
  }

  @unsafe
  @SuppressWarnings("unchecked")
  public <T, U> Option<U> mapBody(Function<T, U> mapper) {
    return body.map(b -> (T) b).map(mapper);
  }

  public Response ok(Object body) {
    return new Response(request, Status.OK, Option.of(body));
  }

  public Response noContent() {
    return new Response(request, Status.NO_CONTENT, Option.none());
  }

  public Response notFound() {
    return new Response(request, Status.NOT_FOUND, Option.none());
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
