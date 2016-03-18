package com.github.ggalmazor.jfry;

class UnknownRequestParamException extends RuntimeException {
  private final Request request;

  UnknownRequestParamException(Request request, String name) {
    super("Param " + name + " is unknown");
    this.request = request;
  }
}
