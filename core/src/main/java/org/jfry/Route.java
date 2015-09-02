package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Route implements Handler, Predicate<Request> {
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

  @Override
  public boolean test(Request request) {
    if (this.method != request.getMethod())
      return false;

    List<String> ourParts = List.of(this.path.split("/"));
    List<String> inputParts = List.of(request.getPath().split("/"));
    List<Tuple2<String, String>> zip = ourParts.zip(inputParts);
    if (zip.length() != ourParts.length())
      return false;

    return zip.map(t -> t._1.startsWith(":") || t._1.equals(t._2)).fold(true, Boolean::logicalAnd);
  }

  @Override
  public Response apply(Request request) {
    List<String> ourParts = List.of(this.path.split("/"));
    List<String> inputParts = List.of(request.getPath().split("/"));
    Map<String, String> pathParams = ourParts.zip(inputParts)
        .filter(t -> t._1.startsWith(":"))
        .map(t -> new Tuple2<>(t._1.substring(1), t._2))
        .toJavaMap(Function.identity());

    return handler.apply(request.withPathParams(pathParams));
  }
}