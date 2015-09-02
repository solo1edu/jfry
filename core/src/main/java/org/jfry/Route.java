package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class Route implements Handler {
  private final HttpMethod method;
  private final String path;
  private final Handler handler;
  private final List<String> parts;
  private final List<Condition> conditions;

  public Route(HttpMethod method, String path, Handler handler, List<Condition> conditions) {
    this.method = method;
    this.path = path;
    this.handler = handler;
    this.conditions = conditions;
    parts = List.of(this.path.split("/"));
  }

  public static Route options(String path, Handler handler) {
    return new Route(HttpMethod.OPTIONS, path, handler, List.nil());
  }

  public static Route get(String path, Handler handler) {
    return new Route(HttpMethod.GET, path, handler, List.nil());
  }

  public static Route head(String path, Handler handler) {
    return new Route(HttpMethod.HEAD, path, handler, List.nil());
  }

  public static Route post(String path, Handler handler) {
    return new Route(HttpMethod.POST, path, handler, List.nil());
  }

  public static Route put(String path, Handler handler) {
    return new Route(HttpMethod.PUT, path, handler, List.nil());
  }

  public static Route delete(String path, Handler handler) {
    return new Route(HttpMethod.DELETE, path, handler, List.nil());
  }

  public static Route trace(String path, Handler handler) {
    return new Route(HttpMethod.TRACE, path, handler, List.nil());
  }

  public static Route connect(String path, Handler handler) {
    return new Route(HttpMethod.CONNECT, path, handler, List.nil());
  }

  public Route withConditions(Condition... conditions) {
    return new Route(method, path, handler, this.conditions.prependAll(List.of(conditions)));
  }

  public boolean test(Request request) {
    return List.<Boolean>nil()
        .prepend(test(request.getMethod()))
        .prepend(test(request.getPath()))
        .prependAll(conditions.map(c -> c.test(request)))
        .fold(true, Boolean::logicalAnd);
  }

  public boolean test(HttpMethod method) {
    return this.method == method;
  }

  public boolean test(String path) {
    List<Tuple2<String, String>> zip = parts.zip(List.of(path.split("/")));
    if (zip.length() != parts.length())
      return false;

    return zip
        .map(t -> t._1.startsWith(":") || t._1.equals(t._2))
        .fold(true, Boolean::logicalAnd);
  }

  @Override
  public Response apply(Request request) {
    Map<String, String> params = new HashMap<>();

    params.putAll(request.getQuery());

    parts.zip(List.of(request.getPath().split("/")))
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

  @FunctionalInterface
  public interface Condition extends Predicate<Request> {
  }
}