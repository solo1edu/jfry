package org.jfry;

import javaslang.collection.List;
import javaslang.control.Try;

public class JFry implements LifeCycle {
  private final List<Route> routes;
  private final JFryServer server;
  private final int port;

  private JFry(List<Route> routes, JFryServer server, int port) {
    this.routes = routes;
    this.server = server;
    this.port = port;
  }

  public static JFry of(JFryServer server, int port) {
    return new JFry(List.nil(), server, port);
  }

  public JFry register(Route... routes) {
    return new JFry(this.routes.prependAll(List.of(routes)), server, port);
  }

  private Response handle(Request request) {
    return List.ofAll(routes)
        .findFirst(r -> r.test(request))
        .map(r -> r.apply(request))
        .orElseGet(() -> request.buildResponse().notFound());
  }

  public Try<JFry> start() {
    return server
        .atPort(port)
        .onRequest(this::handle)
        .start()
        .map(s -> this);
  }

  @Override
  public Try<JFry> stop() {
    return server
        .atPort(port)
        .stop()
        .map(s -> this);
  }
}