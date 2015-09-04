package org.jfry.decorators;

import org.jfry.Response;

import java.util.function.Function;

@FunctionalInterface
public interface ResponseDecorator extends Function<Response, Response> {
  Response apply(Response request);
}
