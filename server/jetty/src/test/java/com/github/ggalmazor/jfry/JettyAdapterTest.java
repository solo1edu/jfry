package com.github.ggalmazor.jfry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JettyAdapterTest {
  @Test
  public void starts_a_Jetty_server_and_uses_it_to_serve_requests() throws Exception {
    JFry.of(new JettyAdapter(), 9999)
        .register(Route.get("/foo", request -> request.buildResponse().ok("bar")))
        .start();

    HttpResponse<String> response = Unirest.get("http://localhost:9999/foo").asString();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    assertThat(response.getBody()).isEqualTo("bar");
  }

  @Test
  public void decodes_query_string_params() throws Exception {
    JFry.of(new JettyAdapter(), 9999)
        .register(Route.get("/foo", request -> request.buildResponse().ok(request.param("bar"))))
        .start();

    HttpResponse<String> response = Unirest.get("http://localhost:9999/foo?bar=123").asString();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    assertThat(response.getBody()).isEqualTo("bar");
  }
}
