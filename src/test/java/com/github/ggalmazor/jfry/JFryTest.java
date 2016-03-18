package com.github.ggalmazor.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import org.assertj.core.api.StrictAssertions;
import org.junit.Before;
import org.junit.Test;

public class JFryTest {

  private TestJFryServer server;

  @Before
  public void setUp() {
    server = new TestJFryServer();
  }

  @Test
  public void resolves_simple_paths() throws Exception {
    JFry.of(server)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .start();

    Response response = server.simulateGet("/foo");

    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(response.hasBody()).isTrue();
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("bar");
  }

  @Test
  public void returns_not_found_response_if_no_handler_is_found() throws Exception {
    JFry.of(server)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .start();

    Response response = server.simulateGet("/bar");

    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    StrictAssertions.assertThat(response.hasBody()).isFalse();
  }

  @Test
  public void makes_dynamic_path_part_values_available() throws Exception {
    JFry.of(server)
        .get("/foo/:bar/baz", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo/123/baz");

    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(response.hasBody()).isTrue();
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("123");
  }

  @Test
  public void resolves_handlers_depending_on_http_method() throws Exception {
    JFry.of(server)
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .post("/foo", req -> req.buildResponse().ok("baz"))
        .start();

    Response getResponse = server.simulateGet("/foo");
    Response postResponse = server.simulatePost("/foo");

    StrictAssertions.assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(getResponse.hasBody()).isTrue();
    StrictAssertions.assertThat(getResponse.getBody().get()).isEqualTo("bar");

    StrictAssertions.assertThat(postResponse.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(postResponse.hasBody()).isTrue();
    StrictAssertions.assertThat(postResponse.getBody().get()).isEqualTo("baz");
  }

  @Test
  public void supports_all_http_1_1_methods() throws Exception {
    JFry.of(server)
        .options("/foo", req -> req.buildResponse().ok("bar"))
        .get("/foo", req -> req.buildResponse().ok("bar"))
        .head("/foo", req -> req.buildResponse().ok("bar"))
        .post("/foo", req -> req.buildResponse().ok("bar"))
        .put("/foo", req -> req.buildResponse().ok("bar"))
        .delete("/foo", req -> req.buildResponse().ok("bar"))
        .trace("/foo", req -> req.buildResponse().ok("bar"))
        .connect("/foo", req -> req.buildResponse().ok("bar"))
        .start();

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
      StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
      StrictAssertions.assertThat(response.hasBody()).isTrue();
      StrictAssertions.assertThat(response.getBody().get()).isEqualTo("bar");
    });
  }

  @Test
  public void makes_query_string_params_available() throws Exception {
    JFry.of(server)
        .get("/foo", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo?bar=123");

    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(response.hasBody()).isTrue();
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("123");
  }

  @Test
  public void dynamic_path_parts_have_precedence_over_query_string_params() throws Exception {
    JFry.of(server)
        .get("/foo/:bar/baz", req -> req.buildResponse().ok(req.param("bar")))
        .start();

    Response response = server.simulateGet("/foo/123/baz?bar=456");

    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(response.hasBody()).isTrue();
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("123");
  }

  @Test
  public void lets_you_add_extra_custom_conditions_for_matching_requests() throws Exception {
    Route.Condition condition = request -> request
        .mapHeader("doge", doge -> doge.equals("very wow"))
        .getOrElse(Boolean.FALSE);

    JFry.of(server)
        .register(Route.get("/foo", req -> req.buildResponse().ok("bar")).withConditions(condition))
        .start();

    Response shouldNotWorkResponse = server.simulateGet("/foo");
    Response shouldWorkResponse = server.simulateGet("/foo", new Tuple2<>("doge", "very wow"));

    StrictAssertions.assertThat(shouldNotWorkResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND);
    StrictAssertions.assertThat(shouldWorkResponse.getStatus()).isEqualTo(Response.Status.OK);
  }

  @Test
  public void makes_request_body_available() {
    JFry.of(server)
        .post("/foo", request -> request.buildResponse().ok(request.getBody()))
        .start();

    Response response = server.simulatePost("/foo", "Very wow, much fancy");
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("Very wow, much fancy");
  }

  @Test
  public void supports_PATCH_non_standard_method() throws Exception {
    JFry.of(server)
        .patch("/foo", request -> request.buildResponse().ok(request.getBody()))
        .start();
    Response response = server.simulatePatch("/foo", "very method");
    StrictAssertions.assertThat(response.getStatus()).isEqualTo(Response.Status.OK);
    StrictAssertions.assertThat(response.getBody().get()).isEqualTo("very method");
  }
}
