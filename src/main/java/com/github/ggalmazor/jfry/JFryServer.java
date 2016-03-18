package com.github.ggalmazor.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Option;

import java.util.function.Function;

public interface JFryServer extends LifeCycle<JFryServer> {

  default Map<String, String> decodeQueryString(String queryString) {
    return Option.of(queryString)
        .map(qs -> qs.split("&"))
        .map(List::of)
        .getOrElse(List::empty)
        .map(pair -> pair.split("="))
        .toMap(parts -> new Tuple2<>(parts[0], parts[1]));
  }

  JFryServer onRequest(ServerHandler handler);

  class ServerHandler {
    private final Handler handler;
    private final Function<Request, List<HttpMethod>> methodCollector;

    public ServerHandler(Handler handler, Function<Request, List<HttpMethod>> methodCollector) {
      this.handler = handler;
      this.methodCollector = methodCollector;
    }

    Response handle(Request request) {
      return handler.apply(request);
    }


    List<HttpMethod> collectAvailableMethods(Request request) {
      return methodCollector.apply(request);
    }
  }

}
