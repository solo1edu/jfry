package com.github.ggalmazor.jfry;

import com.github.ggalmazor.jfry.*;
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
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JettyAdapter implements JFryServer {
  private final Server server;

  public JettyAdapter() {
    this.server = new Server();
  }

  @Override
  public JettyAdapter atPort(int port) {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    return this;
  }

  @Override
  public JettyAdapter onRequest(Handler handler) {
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String path, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Request creation
        HttpMethod method = HttpMethod.valueOf(baseRequest.getMethod().toUpperCase());

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
          String name = headerNames.nextElement();
          headers.put(name, request.getHeader(name));
        }

        Map<String, String> query = decodeQueryString(request.getQueryString());

        Option<Object> body = Try.of(request::getInputStream).mapTry(IOUtils::toString).map(b -> (Object) b).toOption();

        Request jfryRequest = Request.of(method, request.getPathInfo(), headers, query, body);

        // Response creation
        Response jfryResponse = handler.apply(jfryRequest);

        // Response return
        response.setStatus(jfryResponse.getStatus().getCode());
        headers.keySet().forEach(name -> response.addHeader(name, headers.get(name)));
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "");
        response.addHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", String.join(",", headers.keySet()));
        jfryResponse.ifHasBody(_body -> Try.run(() -> {
          PrintWriter writer = response.getWriter();
          writer.write((String) _body);
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