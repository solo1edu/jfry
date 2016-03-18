package com.github.ggalmazor.jfry;

import com.github.ggalmazor.picorouter.PicoRouter;
import javaslang.collection.List;
import javaslang.collection.Map;

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
    return new Route(HttpMethod.OPTIONS, PicoRouter.of(path), handler, List.empty());
  }

  public static Route get(String path, Handler handler) {
    return new Route(HttpMethod.GET, PicoRouter.of(path), handler, List.empty());
  }

  public static Route head(String path, Handler handler) {
    return new Route(HttpMethod.HEAD, PicoRouter.of(path), handler, List.empty());
  }

  public static Route post(String path, Handler handler) {
    return new Route(HttpMethod.POST, PicoRouter.of(path), handler, List.empty());
  }

  public static Route put(String path, Handler handler) {
    return new Route(HttpMethod.PUT, PicoRouter.of(path), handler, List.empty());
  }

  public static Route delete(String path, Handler handler) {
    return new Route(HttpMethod.DELETE, PicoRouter.of(path), handler, List.empty());
  }

  public static Route trace(String path, Handler handler) {
    return new Route(HttpMethod.TRACE, PicoRouter.of(path), handler, List.empty());
  }

  public static Route connect(String path, Handler handler) {
    return new Route(HttpMethod.CONNECT, PicoRouter.of(path), handler, List.empty());
  }

  public static Route patch(String path, Handler handler) {
    return new Route(HttpMethod.PATCH, PicoRouter.of(path), handler, List.empty());
  }

  HttpMethod getMethod() {
    return method;
  }

  public Route withConditions(Condition... conditions) {
    return new Route(method, router, handler, this.conditions.prependAll(List.of(conditions)));
  }

  public boolean test(Request request) {
    return List.<Boolean>empty()
        .prepend(testMethod(request))
        .prepend(testPath(request))
        .prependAll(conditions.map(c -> c.test(request)))
        .fold(true, Boolean::logicalAnd);
  }

  private boolean testMethod(Request request) {
    return this.method == request.getMethod();
  }

  public boolean testPath(Request request) {
    return router.matches(request.getPath());
  }

  public Response apply(Request request) {

    Map<String, String> params = router.parse(request.getPath()).merge(request.getQuery());


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