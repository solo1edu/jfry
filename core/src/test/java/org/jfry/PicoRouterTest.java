package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class PicoRouterTest {

  @Test
  public void matches_exact_paths() throws Exception {
    PR pr = PR.of("/foo/bar/baz");

    assertThat(pr.test("/foo/bar/baz")).isTrue();
    assertThat(pr.test("/foo/baz/bar")).isFalse();
    assertThat(pr.test("/foo/bar")).isFalse();
  }

  @Test
  public void matches_dynamic_paths() throws Exception {
    PR pr = PR.of("/foo/:bar/baz");

    assertThat(pr.test("/foo/bar/baz")).isTrue();
    assertThat(pr.test("/foo/123/baz")).isTrue();
  }

  @Test
  public void returns_captured_values_on_dynamic_path_parts() throws Exception {
    PR pr = PR.of("/foo/:bar/baz");

    Map<String, String> values = pr.apply("/foo/123/baz");
    assertThat(values.get("bar")).isEqualTo("123");
  }

  private static class PR implements Predicate<String>, Function<String, Map<String, String>> {
    private final List<String> parts;

    public PR(List<String> parts) {
      this.parts = parts;
    }

    public static PR of(String path) {
      return new PR(List.of(path.split("/")));
    }

    @Override
    public boolean test(String path) {
      List<Tuple2<String, String>> zipped = parts.zip(PR.of(path).parts);
      return zipped.length() == parts.length()
          && zipped.map(t -> isDynamic(t) || isEqual(t)).fold(true, Boolean::logicalAnd);
    }

    @Override
    public Map<String, String> apply(String path) {
      return parts.zip(PR.of(path).parts)
          .filter(this::isDynamic)
          .map(t -> t.<String, String>map(l -> l.substring(1), r -> r))
          .toJavaMap(Function.identity());
    }

    private boolean isDynamic(Tuple2<String,String> t) {
      return t._1.startsWith(":");
    }

    private boolean isEqual(Tuple2<String, String> t) {
      return t._1.equals(t._2);
    }
  }
}
