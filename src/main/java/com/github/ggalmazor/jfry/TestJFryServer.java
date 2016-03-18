package com.github.ggalmazor.jfry;

import javaslang.Tuple2;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Option;

public class TestJFryServer implements JFryServer {
  private ServerHandler handler;

  private Response simulate(HttpMethod method, String uri, Map<String, String> headers, Option<String> body) {
    String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
    Map<String, String> query = uri.contains("?") ? decodeQueryString(uri.substring(uri.indexOf("?") + 1)) : HashMap.empty();
    Request fakeRequest = Request.of(method, path, headers, query, body.map(b -> (Object) b));
    return handler.handle(fakeRequest);
  }

  @SafeVarargs
  public final Response simulateOptions(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.OPTIONS, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulateGet(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.GET, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulateHead(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.HEAD, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulatePost(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.POST, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulatePost(String uri, String body, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.POST, uri, List.of(headers).toMap(t -> t), Option.of(body));
  }

  @SafeVarargs
  public final Response simulatePut(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.PUT, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulateDelete(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.DELETE, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulateTrace(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.TRACE, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulateConnect(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.CONNECT, uri, List.of(headers).toMap(t -> t), Option.none());
  }

  @SafeVarargs
  public final Response simulatePatch(String uri, String body, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.PATCH, uri, List.of(headers).toMap(t -> t), Option.of(body));
  }

  @Override
  public JFryServer start() {
    return this;
  }

  @Override
  public JFryServer stop() {
    return this;
  }

  @Override
  public JFryServer onRequest(ServerHandler handler) {
    this.handler = handler;
    return this;
  }

}