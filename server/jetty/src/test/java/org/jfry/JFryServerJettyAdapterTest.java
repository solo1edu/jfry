package org.jfry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFryServerJettyAdapterTest {
  @Test
  public void starts_a_Jetty_server_and_uses_it_to_serve_requests() throws Exception {
    JFry.of(new JFryServerJettyAdapter(), 8080)
        .register(Route.get("/foo", request -> request.buildResponse().ok("bar")))
        .start();

    HttpResponse<String> response = Unirest.get("http://localhost:8080/foo").asString();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    assertThat(response.getBody()).isEqualTo("bar");
  }
}
