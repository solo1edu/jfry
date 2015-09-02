package org.jfry;

import javaslang.control.Try;

import java.util.HashMap;
import java.util.Map;

public class TestJFryServer implements JFryServer {
  private Handler handler;

  private Response simulate(HttpMethod method, String uri) {
    String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
    Map<String, String> query = uri.contains("?") ? decodeQueryString(uri.substring(uri.indexOf("?")+1)) : new HashMap<>();
    Request fakeRequest = Request.of(method, path, new HashMap<>(), query, () -> null);
    return handler.apply(fakeRequest);
  }

  public Response simulateOptions(String uri) {
    return simulate(HttpMethod.OPTIONS, uri);
  }

  public Response simulateGet(String uri) {
    return simulate(HttpMethod.GET, uri);
  }

  public Response simulateHead(String uri) {
    return simulate(HttpMethod.HEAD, uri);
  }

  public Response simulatePost(String uri) {
    return simulate(HttpMethod.POST, uri);
  }

  public Response simulatePut(String uri) {
    return simulate(HttpMethod.PUT, uri);
  }

  public Response simulateDelete(String uri) {
    return simulate(HttpMethod.DELETE, uri);
  }

  public Response simulateTrace(String uri) {
    return simulate(HttpMethod.TRACE, uri);
  }

  public Response simulateConnect(String uri) {
    return simulate(HttpMethod.CONNECT, uri);
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