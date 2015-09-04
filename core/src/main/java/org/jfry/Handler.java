package org.jfry;

import org.jfry.decorators.RequestDecorator;
import org.jfry.decorators.ResponseDecorator;

import java.util.function.Function;

@FunctionalInterface
public interface Handler extends Function<Request, Response> {
  Response apply(Request request);

  default Handler andThen(ResponseDecorator f) {
    return request -> f.apply(apply(request));
  }

  default Handler compose(RequestDecorator decorator) {
    return request -> apply(decorator.apply(request));
  }

  static Handler of(Function<Request, Response> handlerLikeFn) {
    return handlerLikeFn::apply;
  }
}
