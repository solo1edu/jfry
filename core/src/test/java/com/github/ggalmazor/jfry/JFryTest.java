package com.github.ggalmazor.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class JFryTest {

  private TestJFryServer server;

  @Before
  public void setUp() {
    server = new TestJFryServer();
  }

  @Test
  public void resolves_simple_paths() throws Exception {
    JFry.of(server, 8080)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .start();

    Response response = server.simulateGet("/foo");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("bar");
  }

  @Test
  public void returns_not_found_response_if_no_handler_is_found() throws Exception {
    JFry.of(server, 8080)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .start();

    Response response = server.simulateGet("/bar");

    assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    assertThat(response.hasBody()).isFalse();
  }

  @Test
  public void makes_dynamic_path_part_values_available() throws Exception {
    JFry.of(server, 8080)
        .get("/foo/:bar/baz", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo/123/baz");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("123");
  }

  @Test
  public void resolves_handlers_depending_on_http_method() throws Exception {
    JFry.of(server, 8080)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .post("/foo", req -> req.buildResponse().ok("baz"))
        .start();

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
            Route.options("/foo", req -> req.buildResponse().ok("bar")),
            Route.get("/foo", req -> req.buildResponse().ok("bar")),
            Route.head("/foo", req -> req.buildResponse().ok("bar")),
            Route.post("/foo", req -> req.buildResponse().ok("bar")),
            Route.put("/foo", req -> req.buildResponse().ok("bar")),
            Route.delete("/foo", req -> req.buildResponse().ok("bar")),
            Route.trace("/foo", req -> req.buildResponse().ok("bar")),
            Route.connect("/foo", req -> req.buildResponse().ok("bar"))
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

  @Test
  public void makes_query_string_params_available() throws Exception {
    JFry.of(server, 8080)
        .get("/foo", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo?bar=123");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("123");
  }

  @Test
  public void dynamic_path_parts_have_precedence_over_query_string_params() throws Exception {
    JFry.of(server, 8080)
        .get("/foo/:bar/baz", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo/123/baz?bar=456");

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    assertThat(response.hasBody()).isTrue();
    assertThat(response.getBody()).isEqualTo("123");
  }

  @Test
  public void lets_you_add_extra_custom_conditions_for_matching_requests() throws Exception {
    Route.Condition condition = request -> request
        .mapHeader("doge", doge -> doge.equals("very wow"))
        .orElse(Boolean.FALSE);

    JFry.of(server, 8080)
        .register(Route.get("/foo", req -> req.buildResponse().ok("bar")).withConditions(condition))
        .start();

    Response shouldNotWorkResponse = server.simulateGet("/foo");
    Response shouldWorkResponse = server.simulateGet("/foo", new Tuple2<>("doge", "very wow"));

    assertThat(shouldNotWorkResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    assertThat(shouldWorkResponse.getStatus()).isEqualTo(Response.Status.OK);
  }

  @Test
  public void makes_request_body_available() {
    JFry.of(server, 8080)
        .post("/foo", request -> request.buildResponse().ok(request.getBody()))
        .start();

    Response response = server.simulatePost("/foo", "Very wow, much fancy");
    assertThat(response.getBody()).isEqualTo("Very wow, much fancy");
  }


}
