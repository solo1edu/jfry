package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Route implements Handler {
  private final HttpMethod method;
  private final String path;
  private final Handler handler;

  public Route(HttpMethod method, String path, Handler handler) {
    this.method = method;
    this.path = path;
    this.handler = handler;
  }

  public static Route options(String path, Handler handler) {
    return new Route(HttpMethod.OPTIONS, path, handler);
  }

  public static Route get(String path, Handler handler) {
    return new Route(HttpMethod.GET, path, handler);
  }

  public static Route head(String path, Handler handler) {
    return new Route(HttpMethod.HEAD, path, handler);
  }

  public static Route post(String path, Handler handler) {
    return new Route(HttpMethod.POST, path, handler);
  }

  public static Route put(String path, Handler handler) {
    return new Route(HttpMethod.PUT, path, handler);
  }

  public static Route delete(String path, Handler handler) {
    return new Route(HttpMethod.DELETE, path, handler);
  }

  public static Route trace(String path, Handler handler) {
    return new Route(HttpMethod.TRACE, path, handler);
  }

  public static Route connect(String path, Handler handler) {
    return new Route(HttpMethod.CONNECT, path, handler);
  }

  public boolean test(Request request) {
    return test(request.getMethod()) && test(request.getPath());
  }

  public boolean test(HttpMethod method) {
    return this.method == method;
  }

  public boolean test(String path) {
    List<String> ourParts = List.of(this.path.split("/"));
    List<String> inputParts = List.of(path.split("/"));
    List<Tuple2<String, String>> zip = ourParts.zip(inputParts);
    if (zip.length() != ourParts.length())
      return false;

    return zip.map(t -> t._1.startsWith(":") || t._1.equals(t._2)).fold(true, Boolean::logicalAnd);
  }

  @Override
  public Response apply(Request request) {
    Map<String, String> params = new HashMap<>();

    params.putAll(request.getQuery());

    List<String> ourParts = List.of(this.path.split("/"));
    List<String> inputParts = List.of(request.getPath().split("/"));
    ourParts.zip(inputParts)
        .filter(t -> t._1.startsWith(":"))
        .map(t -> new Tuple2<>(t._1.substring(1), t._2))
        .forEach(t -> params.put(t._1, t._2));

    return handler.apply(request.withParams(params));
  }

  @Override
  public String toString() {
    return "Route " + method + ":" + path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Route route = (Route) o;
    return Objects.equals(method, route.method) &&
        Objects.equals(path, route.path) &&
        Objects.equals(handler, route.handler);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, path, handler);
  }
}