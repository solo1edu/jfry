package com.github.ggalmazor.jfry;

import javaslang.collection.List;

public class JFry implements LifeCycle<JFry> {
  private final List<Route> routes;
  private final JFryServer server;

  private JFry(List<Route> routes, JFryServer server) {
    this.routes = routes;
    this.server = server;
  }

  public static JFry of(JFryServer server) {
    return new JFry(List.empty(), server);
  }

  public JFry register(Route... routes) {
    return new JFry(this.routes.prependAll(List.of(routes)), server);
  }

  public JFry options(String path, Handler handler) {
    return new JFry(routes.prepend(Route.options(path, handler)), server);
  }

  public JFry get(String path, Handler handler) {
    return new JFry(routes.prepend(Route.get(path, handler)), server);
  }

  public JFry head(String path, Handler handler) {
    return new JFry(routes.prepend(Route.head(path, handler)), server);
  }

  public JFry post(String path, Handler handler) {
    return new JFry(routes.prepend(Route.post(path, handler)), server);
  }

  public JFry put(String path, Handler handler) {
    return new JFry(routes.prepend(Route.put(path, handler)), server);
  }

  public JFry delete(String path, Handler handler) {
    return new JFry(routes.prepend(Route.delete(path, handler)), server);
  }

  public JFry trace(String path, Handler handler) {
    return new JFry(routes.prepend(Route.trace(path, handler)), server);
  }

  public JFry connect(String path, Handler handler) {
    return new JFry(routes.prepend(Route.connect(path, handler)), server);
  }

  public JFry patch(String path, Handler handler) {
    return new JFry(routes.prepend(Route.patch(path, handler)), server);
  }

  private Response handle(Request request) {
    return List.ofAll(routes)
        .find(r -> r.test(request))
        .map(r -> r.apply(request))
        .getOrElse(() -> request.buildResponse().notFound());
  }

  private List<HttpMethod> collectAvailableMethods(Request request) {
    return List.ofAll(routes)
        .filter(r -> r.testPath(request))
        .map(Route::getMethod);
  }

  @Override
  public JFry start() {
    server.onRequest(new JFryServer.ServerHandler(this::handle, this::collectAvailableMethods)).start();
    return this;
  }

  @Override
  public JFry stop() {
    server.stop();
    return this;
  }
}