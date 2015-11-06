package com.github.ggalmazor.jfry;

import com.github.ggalmazor.jfry.PicoRouter;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PicoRouterTest {

  @Test
  public void matches_exact_paths() throws Exception {
    PicoRouter pr = PicoRouter.of("/foo/bar/baz");

    assertThat(pr.test("/foo/bar/baz")).isTrue();
    assertThat(pr.test("/foo/baz/bar")).isFalse();
    assertThat(pr.test("/foo/bar")).isFalse();
  }

  @Test
  public void matches_dynamic_paths() throws Exception {
    PicoRouter pr = PicoRouter.of("/foo/:bar/baz");

    assertThat(pr.test("/foo/bar/baz")).isTrue();
    assertThat(pr.test("/foo/123/baz")).isTrue();
  }

  @Test
  public void returns_captured_values_on_dynamic_path_parts() throws Exception {
    PicoRouter pr = PicoRouter.of("/foo/:bar/baz");

    Map<String, String> values = pr.apply("/foo/123/baz");
    assertThat(values.get("bar")).isEqualTo("123");
  }

}
