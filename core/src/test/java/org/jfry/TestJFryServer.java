package org.jfry;

import javaslang.control.Option;
import javaslang.control.Try;
import org.jfry.HttpMethod;
import org.jfry.JFryServer;
import org.jfry.Request;
import org.jfry.Response;

import java.util.HashMap;

public class TestJFryServer implements JFryServer {
  private Handler handler;

  public Response simulateOptions(String path) {
    Request fakeRequest = new Request(HttpMethod.OPTIONS, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulateGet(String path) {
    Request fakeRequest = new Request(HttpMethod.GET, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulateHead(String path) {
    Request fakeRequest = new Request(HttpMethod.HEAD, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulatePost(String path) {
    Request fakeRequest = new Request(HttpMethod.POST, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulatePut(String path) {
    Request fakeRequest = new Request(HttpMethod.PUT, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulateDelete(String path) {
    Request fakeRequest = new Request(HttpMethod.DELETE, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulateTrace(String path) {
    Request fakeRequest = new Request(HttpMethod.TRACE, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
  }

  public Response simulateConnect(String path) {
    Request fakeRequest = new Request(HttpMethod.CONNECT, path, new HashMap<>(), new HashMap<>(), Option.none());
    return handler.apply(fakeRequest);
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