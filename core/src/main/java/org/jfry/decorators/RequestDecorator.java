package org.jfry.decorators;

import org.jfry.Request;

import java.util.function.Function;

@FunctionalInterface
public interface RequestDecorator extends Function<Request, Request> {
  Request apply(Request request);
}
