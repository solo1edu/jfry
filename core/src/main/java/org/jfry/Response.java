package org.jfry;

import javaslang.control.Option;

import java.util.function.Consumer;

class Response {
  private final Request request;
  private final Status status;
  private final Option<String> body;

  Response(Request request, Status status, Option<String> body) {
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

  public String getBody() {
    return body.orElse("");
  }

  public boolean hasBody() {
    return body.isDefined() && !body.map(String::isEmpty).orElse(true);
  }

  public void ifHasBody(Consumer<String> consumer) {
    body.forEach(consumer);
  }

  public Response ok(String body) {
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
