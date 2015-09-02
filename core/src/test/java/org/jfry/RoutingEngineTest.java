package org.jfry;

import javaslang.collection.List;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class RoutingEngineTest {

  private TestJFryServer server;

  @Before
  public void setUp() {
    server = new TestJFryServer();
  }

  @Test
  public void resolves_simple_paths() throws Exception {
    JFry.of(server, 8080)
        .register(Route.get("/foo", (Request req) -> req.buildResponse().ok("bar")))
        .start();

    Response response = server.simulateGet("/foo");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("bar");
  }

  @Test
  public void returns_not_found_response_if_no_handler_is_found() throws Exception {
    JFry.of(server, 8080)
        .register(Route.get("/foo", (Request req) -> req.buildResponse().ok("bar")))
        .start();

    Response response = server.simulateGet("/bar");

    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    assertThat(response.hasBody()).isFalse();
  }

  @Test
  public void captures_dynamic_path_part_values_and_makes_them_available() throws Exception {
    JFry.of(server, 8080)
        .register(Route.get("/foo/:bar/baz", (Request req) -> req.buildResponse().ok(req.param("bar"))))
        .start();

    Response response = server.simulateGet("/foo/123/baz");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("123");
  }

  @Test
  public void resolves_handlers_depending_on_http_method() throws Exception {
    JFry.of(server, 8080)
        .register(
            Route.get("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.post("/foo", (Request req) -> req.buildResponse().ok("baz"))
        ).start();

    Response getResponse = server.simulateGet("/foo");
    Response postResponse = server.simulatePost("/foo");

    assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(getResponse.hasBody()).isTrue();
    assertThat(getResponse.getBody()).isEqualTo("bar");

    assertThat(postResponse.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(postResponse.hasBody()).isTrue();
    assertThat(postResponse.getBody()).isEqualTo("baz");
  }

  @Test
  public void supports_all_http_1_1_methods() throws Exception {
    JFry.of(server, 8080)
        .register(
            Route.options("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.get("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.head("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.post("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.put("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.delete("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.trace("/foo", (Request req) -> req.buildResponse().ok("bar")),
            Route.connect("/foo", (Request req) -> req.buildResponse().ok("bar"))
        ).start();

    List.of(
        server.simulateOptions("/foo"),
        server.simulateGet("/foo"),
        server.simulateHead("/foo"),
        server.simulatePost("/foo"),
        server.simulatePut("/foo"),
        server.simulateDelete("/foo"),
        server.simulateTrace("/foo"),
        server.simulateConnect("/foo")
    ).forEach(response -> {
      assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
      assertThat(response.hasBody()).isTrue();
      assertThat(response.getBody()).isEqualTo("bar");
    });
  }
}
