package org.jfry;

import javaslang.collection.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class Route {
  private final HttpMethod method;
  private final PicoRouter router;
  private final Handler handler;
  private final List<Condition> conditions;

  private Route(HttpMethod method, PicoRouter router, Handler handler, List<Condition> conditions) {
    this.method = method;
    this.router = router;
    this.handler = handler;
    this.conditions = conditions;
  }

  public static Route options(String path, Handler handler) {
    return new Route(HttpMethod.OPTIONS, PicoRouter.of(path), handler, List.nil());
  }

  public static Route get(String path, Handler handler) {
    return new Route(HttpMethod.GET, PicoRouter.of(path), handler, List.nil());
  }

  public static Route head(String path, Handler handler) {
    return new Route(HttpMethod.HEAD, PicoRouter.of(path), handler, List.nil());
  }

  public static Route post(String path, Handler handler) {
    return new Route(HttpMethod.POST, PicoRouter.of(path), handler, List.nil());
  }

  public static Route put(String path, Handler handler) {
    return new Route(HttpMethod.PUT, PicoRouter.of(path), handler, List.nil());
  }

  public static Route delete(String path, Handler handler) {
    return new Route(HttpMethod.DELETE, PicoRouter.of(path), handler, List.nil());
  }

  public static Route trace(String path, Handler handler) {
    return new Route(HttpMethod.TRACE, PicoRouter.of(path), handler, List.nil());
  }

  public static Route connect(String path, Handler handler) {
    return new Route(HttpMethod.CONNECT, PicoRouter.of(path), handler, List.nil());
  }

  public Route withConditions(Condition... conditions) {
    return new Route(method, router, handler, this.conditions.prependAll(List.of(conditions)));
  }

  public boolean test(Request request) {
    return List.<Boolean>nil()
        .prepend(test(request.getMethod()))
        .prepend(test(request.getPath()))
        .prependAll(conditions.map(c -> c.test(request)))
        .fold(true, Boolean::logicalAnd);
  }

  private boolean test(HttpMethod method) {
    return this.method == method;
  }

  private boolean test(String path) {
    return router.test(path);
  }

  public Response apply(Request request) {
    Map<String, String> params = new HashMap<>();

    params.putAll(request.getQuery());

    params.putAll(router.apply(request.getPath()));

    return handler.apply(request.withParams(params));
  }

  @Override
  public String toString() {
    return "Route " + method + ":" + router;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Route route = (Route) o;
    return Objects.equals(method, route.method) &&
        Objects.equals(router, router) &&
        Objects.equals(handler, route.handler);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, router, handler);
  }

  @FunctionalInterface
  public interface Condition extends Predicate<Request> {
  }
}