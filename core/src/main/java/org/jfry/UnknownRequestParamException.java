package org.jfry;

public class UnknownRequestParamException extends RuntimeException {
  public UnknownRequestParamException(String name) {
    super("Param " + name + " is unknown");
  }
}
