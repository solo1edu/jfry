package com.github.ggalmazor.jfry.decorators;

import com.github.ggalmazor.jfry.Response;

import java.util.function.Function;

@FunctionalInterface
public interface ResponseDecorator extends Function<Response, Response> {
  Response apply(Response request);
}
