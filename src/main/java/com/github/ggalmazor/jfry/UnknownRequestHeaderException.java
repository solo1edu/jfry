package com.github.ggalmazor.jfry;

class UnknownRequestHeaderException extends RuntimeException {
  private final Request request;

  UnknownRequestHeaderException(Request request, String name) {
    super("Header " + name + " is unknown");
    this.request = request;
  }
}
