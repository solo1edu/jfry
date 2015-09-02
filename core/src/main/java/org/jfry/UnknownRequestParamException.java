package org.jfry;

public class UnknownRequestParamException extends RuntimeException {
  private final Request request;

  public UnknownRequestParamException(Request request, String name) {
    super("Param " + name + " is unknown");
    this.request = request;
  }
}
