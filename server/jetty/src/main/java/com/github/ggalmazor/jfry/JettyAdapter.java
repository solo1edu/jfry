package com.github.ggalmazor.jfry;

import javaslang.collection.List;
import javaslang.control.Match;
import javaslang.control.Option;
import javaslang.control.Try;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JettyAdapter implements JFryServer {
  private final Server server;
  private final int port;
  private final List<String> corsExposeHeaders;


  public JettyAdapter(int port, List<String> corsExposeHeaders) {
    this.port = port;
    this.corsExposeHeaders = corsExposeHeaders;
    this.server = new Server();
  }

  @Override
  public JettyAdapter onRequest(ServerHandler handler) {
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String path, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Headers headers = getRequestHeaders(request);
        Request jfryRequest = buildJFryRequest(request, headers);
        Response jfryResponse = handler.handle(jfryRequest);

        // Response return
        response.setStatus(jfryResponse.getStatus().getCode());
        response.addHeader("Access-Control-Allow-Origin", headers.getOption("Origin").orElse("*"));
        response.addHeader("Access-Control-Expose-Headers", corsExposeHeaders.mkString(","));
        response.addHeader("Access-Control-Allow-Methods", handler.collectAvailableMethods(jfryRequest).mkString(","));
        headers.forEach("Access-Control-Request-Headers", allowHeaders -> response.addHeader("Access-Control-Allow-Headers", allowHeaders));
        jfryResponse.forEachHeader(response::setHeader);
        jfryResponse.forEachBody(body -> {
          if (body instanceof InputStream) {
            Try.run(() -> IOUtils.copy(((InputStream) body), response.getOutputStream())).get();
          } else {
            byte[] bytes = Match
                .whenApplicable((byte[] b) -> b).thenApply()
                .whenApplicable(ByteBuffer::array).thenApply()
                .otherwise(b -> Try.of(() -> b.toString().getBytes("utf-8")).get())
                .apply(body);
            Try.run(() -> IOUtils.write(bytes, response.getOutputStream())).get();
          }
        });
        baseRequest.setHandled(true);
      }
    });
    return this;
  }

  private Request buildJFryRequest(HttpServletRequest request, Headers headers) {
    // Request creation
    HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

    Map<String, String> query = decodeQueryString(request.getQueryString());

    Option<Object> body = Try.of(request::getInputStream)
        .mapTry(IOUtils::toString)
        .map(b -> (Object) b)
        .toOption();

    return Request.of(method, request.getPathInfo(), headers.toMap(), query, body);
  }

  private Headers getRequestHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      headers.put(name, request.getHeader(name));
    }
    return new Headers(headers);
  }

  @Override
  public Try<JFryServer> start() {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    return Try.of(() -> {
      server.start();
//      server.join();
      return this;
    });
  }

  @Override
  public Try<JFryServer> stop() {
    return Try.run(server::stop).mapTry(v -> this);
  }

  public static class Headers {
    private final Map<String, String> storage;

    public Headers(Map<String, String> storage) {
      this.storage = storage;
    }

    public Option<String> getOption(String key) {
      return Option.of(storage.get(key));
    }

    public Map<String, String> toMap() {
      return storage;
    }

    public void forEach(String key, Consumer<String> consumer) {
      getOption(key).forEach(consumer);
    }
  }
}