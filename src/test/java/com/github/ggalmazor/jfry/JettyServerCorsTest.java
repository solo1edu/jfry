package com.github.ggalmazor.jfry;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javaslang.collection.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;


public class JettyServerCorsTest {

  private static final int PORT = 9999;
  private static final String REQUEST_URL = "http://localhost:" + PORT + "/foo";
  private static final String ALLOWED_ORIGIN = "http://allowed.iam";
  private static final String NON_ALLOWED_ORIGIN = "http://not.allowed.iam";
  private static JFry jfry;

  @BeforeClass
  public static void setUp() {
    JettyServer server = JettyServer.of(PORT, ALLOWED_ORIGIN);

    jfry = JFry.of(server)
        .get("/foo", req -> req.buildResponse().withHeaders("X-custom", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).ok("hu ha"))
        .start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    jfry.stop();
  }

  @Test
  public void non_cors_compliant_request_gets_a_401_with_cors_info() throws Exception {
    HttpResponse<String> response = Unirest.get(REQUEST_URL)
        .headers(HashMap.of("Origin", NON_ALLOWED_ORIGIN).toJavaMap())
        .asString();
    Headers headers = response.getHeaders();

    assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getCode());
    assertThat(headers.get("access-control-allow-origin")).contains(ALLOWED_ORIGIN);
    assertThat(headers.get("access-control-allow-headers")).contains("*");
    assertThat(headers.get("access-control-allow-methods")).contains("*");
    assertThat(headers.get("access-control-expose-headers")).contains("*");
    assertThat(headers.get("access-control-max-age")).contains("1800");
    assertThat(headers.get("access-control-allow-credentials")).contains("true");
  }

  @Test
  public void preflight_request_get_through_and_receive_cors_info() throws Exception {
    HttpResponse<String> response = Unirest.options(REQUEST_URL)
        .headers(HashMap.<String, String>of(
            "Origin", NON_ALLOWED_ORIGIN,
            "Access-Control-Request-Method", "POST"
        ).toJavaMap())
        .asString();
    Headers headers = response.getHeaders();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    assertThat(headers.get("access-control-allow-origin")).contains(ALLOWED_ORIGIN);
    assertThat(headers.get("access-control-allow-headers")).contains("*");
    assertThat(headers.get("access-control-allow-methods")).contains("*");
    assertThat(headers.get("access-control-expose-headers")).contains("*");
    assertThat(headers.get("access-control-max-age")).contains("1800");
    assertThat(headers.get("access-control-allow-credentials")).contains("true");
  }

  @Test
  public void cors_compliant_requests_get_a_200_with_content_and_cors_info() throws Exception {
    HttpResponse<String> response = Unirest.get(REQUEST_URL)
        .headers(HashMap.of("Origin", ALLOWED_ORIGIN).toJavaMap())
        .asString();
    Headers headers = response.getHeaders();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getCode());
    assertThat(headers.get("access-control-allow-origin")).contains(ALLOWED_ORIGIN);
    assertThat(headers.get("access-control-allow-headers")).contains("*");
    assertThat(headers.get("access-control-allow-methods")).contains("*");
    assertThat(headers.get("access-control-expose-headers")).contains("*");
    assertThat(headers.get("access-control-max-age")).contains("1800");
    assertThat(headers.get("access-control-allow-credentials")).contains("true");
    assertThat(headers.get("x-custom")).isNotEmpty();
    assertThat(response.getBody()).isEqualTo("hu ha");
  }

}
