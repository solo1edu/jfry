package org.jfry;

public class UnknownRequestHeaderException extends RuntimeException {
  private final Request request;

  public UnknownRequestHeaderException(Request request, String name) {
    super("Header " + name + " is unknown");
    this.request = request;
  }
}
