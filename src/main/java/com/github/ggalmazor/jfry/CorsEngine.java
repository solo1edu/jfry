package com.github.ggalmazor.jfry;

import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;

class CorsEngine {
  // Request headers
  private static final String ORIGIN_HEADER = "Origin";
  private static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
  public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
  // Response headers
  private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
  private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
  private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
  private static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
  private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
  private static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
  // Implementation constants
  private static final String ANY_ORIGIN = "*";

  private final List<String> allowedOrigins;
  private final List<HttpMethod> allowedMethods;

  public CorsEngine(List<String> allowedOrigins, List<HttpMethod> allowedMethods) {
    this.allowedOrigins = allowedOrigins;
    this.allowedMethods = allowedMethods;
  }

  public Map<String, String> getResponseHeaders(Request request) {
    String fallbackAllowOriginValue = allowedOrigins.mkString(",");
    return HashMap.of(
        ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, isAllowed(request) ?
            request.getHeader(ORIGIN_HEADER).getOrElse(fallbackAllowOriginValue)
            : fallbackAllowOriginValue,
        ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true",
        ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, "*",
        ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "*",
        ACCESS_CONTROL_MAX_AGE_HEADER, "1800",
        ACCESS_CONTROL_ALLOW_METHODS_HEADER, "*"
    );
  }

  public boolean isPreflight(Request request) {
    return request.getMethod() == HttpMethod.OPTIONS && request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER).isDefined();
  }

  public boolean isAllowed(Request request) {
    return isAnyOriginAllowed() || request.getHeader(ORIGIN_HEADER)
        .map(origin -> allowedMethods.contains(request.getMethod()) && (isAnyOriginAllowed() || allowedOrigins.contains(origin)))
        .getOrElse(false);
  }

  private boolean isAnyOriginAllowed() {
    return allowedOrigins.isEmpty() || allowedOrigins.contains(ANY_ORIGIN);
  }
}