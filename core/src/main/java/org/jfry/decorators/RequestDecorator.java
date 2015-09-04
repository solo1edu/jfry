package org.jfry.decorators;

import org.jfry.Request;

import java.util.function.Function;

@FunctionalInterface
public interface RequestDecorator {
  Request apply(Request request);

  default RequestDecorator andThen(Function<Request, Request> fn) {
    return request -> fn.apply(apply(request));
  }
}
