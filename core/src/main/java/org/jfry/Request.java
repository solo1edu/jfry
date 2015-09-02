package org.jfry;

import javaslang.control.Option;

import java.util.HashMap;
import java.util.Map;

class Request {
  private final HttpMethod method;
  private final String path;
  private final Map<String, String> params;
  private final Map<String, String> headers;
  private final Option<Object> body;

  Request(HttpMethod method, String path, Map<String, String> params, Map<String, String> headers, Option<Object> body) {
    this.method = method;
    this.path = path;
    this.params = params;
    this.headers = headers;
    this.body = body;
  }


  static Request withoutBody(HttpMethod method, String path, Map<String, String> params, Map<String, String> headers) {
    return new Request(method, path, params, headers, Option.none());
  }

  Response buildResponse() {
    return Response.from(this);
  }

  String param(String name) {
    return Option
        .of(params.get(name))
        .orElseThrow(() -> new UnknownRequestParamException(name));
  }

  String getPath() {
    return path;
  }

  public HttpMethod getMethod() {
    return method;
  }


  Request withPathParams(Map<String, String> pathParams) {
    Map<String, String> newParams = new HashMap<>();
    newParams.putAll(params);
    newParams.putAll(pathParams);
    return new Request(method, path, newParams, headers, body);
  }
}
