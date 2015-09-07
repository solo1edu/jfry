package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.control.Option;
import javaslang.control.Try;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TestJFryServer implements JFryServer {
  private Handler handler;

  private Response simulate(HttpMethod method, String uri, Map<String, String> headers, Option<String> body) {
    String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
    Map<String, String> query = uri.contains("?") ? decodeQueryString(uri.substring(uri.indexOf("?") + 1)) : new HashMap<>();
    Request fakeRequest = Request.of(method, path, headers, query, body.map(b -> (Object) b));
    return handler.apply(fakeRequest);
  }

  @SafeVarargs
  public final Response simulateOptions(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.OPTIONS, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulateGet(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.GET, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulateHead(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.HEAD, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulatePost(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.POST, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulatePost(String uri, String body, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.POST, uri, List.of(headers).toJavaMap(Function.identity()), Option.of(body));
  }

  @SafeVarargs
  public final Response simulatePut(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.PUT, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulateDelete(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.DELETE, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulateTrace(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.TRACE, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @SafeVarargs
  public final Response simulateConnect(String uri, Tuple2<String, String>... headers) {
    return simulate(HttpMethod.CONNECT, uri, List.of(headers).toJavaMap(Function.identity()), Option.none());
  }

  @Override
  public JFryServer atPort(int port) {
    return this;
  }

  @Override
  public JFryServer onRequest(Handler handler) {
    this.handler = handler;
    return this;
  }

  @Override
  public Try<JFryServer> start() {
    return Try.of(() -> this);
  }

  @Override
  public Try<JFryServer> stop() {
    return Try.of(() -> this);
  }
}