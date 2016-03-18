package com.github.ggalmazor.jfry;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Map;
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
import java.util.ArrayList;
import java.util.Enumeration;

public class JettyServer implements JFryServer {
  private final Server server;
  private final int port;
  private final List<String> allowedOrigins;

  private JettyServer(int port, List<String> allowedOrigins, Server server) {
    this.port = port;
    this.allowedOrigins = allowedOrigins;
    this.server = server;
  }

  public static JettyServer of(int port) {
    return new JettyServer(port, List.empty(), new Server());
  }

  public static JettyServer of(int port, String... allowedOrigins) {
    return new JettyServer(port, List.of(allowedOrigins), new Server());
  }

  @Override
  public JettyServer onRequest(ServerHandler handler) {
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String path, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // 1) Build our response object
        Request jfryRequest = buildJFryRequest(request, getRequestHeaders(request));

        // 2) Have we declared any handler for this request?
        List<HttpMethod> availableMethods = handler.collectAvailableMethods(jfryRequest);
        if (availableMethods.isEmpty()) {
          response.setStatus(Response.Status.NOT_FOUND.getCode());
          baseRequest.setHandled(true);
          return;
        }

        CorsEngine cors = new CorsEngine(allowedOrigins, availableMethods);

        // 3) Prefill cors response headers
        Map<String, String> corsResponseHeaders = cors.getResponseHeaders(jfryRequest);
        corsResponseHeaders.forEach(response::setHeader);

        // 4) If request is a preflight CORS request, return 200
        if (cors.isPreflight(jfryRequest)) {
          response.setStatus(Response.Status.OK.getCode());
          baseRequest.setHandled(true);
          return;
        }

        // 5) If request does not comply with CORS settings, return 401
        if (!cors.isAllowed(jfryRequest)) {
          response.sendError(Response.Status.UNAUTHORIZED.getCode(), Response.Status.UNAUTHORIZED.getMsg());
          baseRequest.setHandled(true);
          return;
        }

        // 6) Handle request
        Response jfryResponse = handler.handle(jfryRequest);

        // 7) Prepare response
        response.setStatus(jfryResponse.getStatus().getCode());

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


  private Request buildJFryRequest(HttpServletRequest request, Map<String, String> headers) {
    HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

    Map<String, String> query = decodeQueryString(request.getQueryString());

    Option<Object> body = Try.of(request::getInputStream)
        .mapTry(IOUtils::toString)
        .map(b -> (Object) b)
        .toOption();

    return Request.of(method, request.getPathInfo(), headers, query, body);
  }

  private Map<String, String> getRequestHeaders(HttpServletRequest request) {
    java.util.List<Tuple2<String, String>> pairs = new ArrayList<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      String value = request.getHeader(name);
      if (value != null && !value.trim().isEmpty())
        pairs.add(Tuple.of(name, value));
    }
    return List.ofAll(pairs).toMap(t -> t);
  }

  @Override
  public JFryServer start() {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    Try.run(server::start).get();
    return this;
  }

  @Override
  public JFryServer stop() {
    Try.run(server::stop);
    return this;
  }

}