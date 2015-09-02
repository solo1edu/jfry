package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.control.Option;

import java.util.Map;
import java.util.function.Function;

public interface JFryServer extends LifeCycle<JFryServer> {

  default Map<String, String> decodeQueryString(String queryString) {
    return Option.of(queryString)
        .map(qs -> qs.split("&"))
        .map(List::of)
        .orElse(List.nil())
        .map(pair -> pair.split("="))
        .map(parts -> new Tuple2<>(parts[0], parts[1]))
        .toJavaMap(Function.identity());
  }

  JFryServer atPort(int port);

  JFryServer onRequest(Handler handler);

}
