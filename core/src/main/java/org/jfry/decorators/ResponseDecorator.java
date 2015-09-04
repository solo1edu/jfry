package org.jfry.decorators;

import org.jfry.Response;

import java.util.function.Function;

@FunctionalInterface
public interface ResponseDecorator {
  Response apply(Response request);

  default ResponseDecorator andThen(Function<Response, Response> fn) {
    return request -> fn.apply(apply(request));
  }
}
