package org.jfry;

import javaslang.control.Option;
import javaslang.control.Try;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JFryServerJettyAdapter implements JFryServer {
  private final Server server;

  public JFryServerJettyAdapter() {
    this.server = new Server();
  }

  @Override
  public JFryServerJettyAdapter atPort(int port) {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    return this;
  }

  @Override
  public JFryServerJettyAdapter onRequest(Handler handler) {
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpMethod method = HttpMethod.valueOf(baseRequest.getMethod().toUpperCase());
        // Params from Request depending on matching route
        Map<String, String> params = new HashMap<>();

        // Request creation
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
          String name = headerNames.nextElement();
          headers.put(name, request.getHeader(name));
        }
        Request jfryRequest = new Request(method, target, params, headers, Option.none());

        // Response creation
        Response jfryResponse = handler.apply(jfryRequest);

        // Response return
        response.setStatus(jfryResponse.getStatus().getCode());
        headers.keySet().forEach(name -> response.addHeader(name, headers.get(name)));
        jfryResponse.ifHasBody(body -> Try.run(() -> {
          PrintWriter writer = response.getWriter();
          writer.write(body);
          writer.flush();
          writer.close();
        }));

        baseRequest.setHandled(true);
      }
    });
    return this;
  }

  @Override
  public Try<JFryServer> start() {
    return Try.of(() -> {
      server.start();
//      server.join();
      return this;
    });
  }

  @Override
  public Try<JFryServer> stop() {
    throw new RuntimeException("not yet implemented");
  }
}