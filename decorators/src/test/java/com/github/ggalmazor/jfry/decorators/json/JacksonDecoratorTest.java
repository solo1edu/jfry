package com.github.ggalmazor.jfry.decorators.json;

import com.github.ggalmazor.jfry.Handler;
import com.github.ggalmazor.jfry.JFry;
import com.github.ggalmazor.jfry.Response;
import com.github.ggalmazor.jfry.TestJFryServer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonDecoratorTest {
  private TestJFryServer server;

  @Before
  public void setUp() {
    server = new TestJFryServer();
  }

  @Test
  public void serializes_Request_body_and_deserializes_Response_body() throws Exception {
    Handler handler = req -> {
      System.out.println("I'm working with a Doge instance");
      System.out.println(req.<Doge>getBody());
      return req.buildResponse().ok(req.<Doge>getBody());
    };

    Handler decoratedHandler = Handler.of(JacksonDecorator.deserialize().andThen(handler).andThen(JacksonDecorator.serialize()));

    JFry.of(server, 8080)
        .post("/foo", decoratedHandler)
        .start();

    Response response = server.simulatePost("/foo", "{\"name\":\"much fancy\",\"sound\":\"wow\"}");

    String body = response.<String>getBody();
    assertThat(body).isEqualTo("{\"name\":\"much fancy\",\"sound\":\"wow\"}");
  }

  @Test
  public void less_verbose_version() throws Exception {
    Handler handler = req -> {
      System.out.println("I'm working with a Doge instance");
      System.out.println(req.<Doge>getBody());
      return req.buildResponse().ok(req.<Doge>getBody());
    };

    JFry.of(server, 8080)
        .post("/foo", JacksonDecorator.wrap(handler))
        .start();

    Response response = server.simulatePost("/foo", "{\"name\":\"much fancy\",\"sound\":\"wow\"}");

    String body = response.<String>getBody();
    assertThat(body).isEqualTo("{\"name\":\"much fancy\",\"sound\":\"wow\"}");
  }

}
