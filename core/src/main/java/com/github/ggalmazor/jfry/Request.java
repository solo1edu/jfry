package com.github.ggalmazor.jfry;

import javaslang.control.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Request {
  private final HttpMethod method;
  private final String path;
  private final Map<String, String> headers;
  private final Map<String, String> query;
  private final Map<String, String> params;
  private final Option<Object> body;

  private Request(HttpMethod method, String path, Map<String, String> headers, Map<String, String> query, Map<String, String> params, Option<Object> body) {
    this.method = method;
    this.path = path;
    this.params = params;
    this.headers = headers;
    this.body = body;
    this.query = query;
  }

  public static Request of(HttpMethod method, String path, Map<String, String> headers, Map<String, String> query, Option<Object> body) {
    return new Request(method, path, headers, query, new HashMap<>(), body);
  }

  public Response buildResponse() {
    return Response.from(this);
  }

  String param(String name) {
    return Option.of(params.get(name)).orElseThrow(() -> new UnknownRequestParamException(this, name));
  }

  public <U> Option<U> mapParam(String name, Function<String, U> mapper) {
    return Option.of(params.get(name)).map(mapper);
  }

  public Map<String, String> getParamsMap() {
    return params;
  }

  public String header(String name) {
    return Option.of(headers.get(name)).orElseThrow(() -> new UnknownRequestHeaderException(this, name));
  }

  public <U> Option<U> mapHeader(String name, Function<String, U> mapper) {
    return Option.of(headers.get(name)).map(mapper);
  }

  public Map<String, String> getHeadersMap() {
    return headers;
  }

  @SuppressWarnings("unchecked")
  public <T, U> Option<U> mapBody(Function<T, U> mapper) {
    return body.map(b -> (T) b).map(mapper);
  }

  String getPath() {
    return path;
  }

  public HttpMethod getMethod() {
    return method;
  }

  Request withParams(Map<String, String> params) {
    return new Request(method, path, headers, query, params, body);
  }

  public Request withBody(Object body) {
    return new Request(method, path, headers, query, params, Option.of(body));
  }

  public Map<String, String> getQuery() {
    return query;
  }

  @SuppressWarnings("unchecked")
  public <T> T getBody() {
    return (T) body.get();
  }
}
