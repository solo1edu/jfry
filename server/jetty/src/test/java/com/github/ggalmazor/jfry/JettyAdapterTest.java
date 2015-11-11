package com.github.ggalmazor.jfry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javaslang.Tuple;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

  @Test
  public void encodes_response_body_as_binary_data_if_required_by_content_type() throws Exception {
    byte[] expectedBytes = Files.readAllBytes(Paths.get(JettyAdapterTest.class.getResource("/image.jpeg").toURI()));
    JFry.of(new JettyAdapter(), 9999)
        .get("/foo", request -> request.buildResponse()
            .withHeaders(Tuple.of("Content-type", "image/jpeg"))
            .ok(expectedBytes))
        .start();
    HttpResponse<InputStream> response = Unirest.get("http://localhost:9999/foo").asBinary();
    byte[] actualBytes = IOUtils.toByteArray(response.getBody());
    assertThat(actualBytes).isEqualTo(expectedBytes);

  }
}
