package org.jfry;

@FunctionalInterface
public interface Handler {
  Response apply(Request request);
}
