package org.jfry;

import javaslang.Tuple2;
import javaslang.collection.List;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class PicoRouter implements Predicate<String>, Function<String, Map<String, String>> {
  private final List<String> parts;

  public PicoRouter(List<String> parts) {
    this.parts = parts;
  }

  public static PicoRouter of(String path) {
    return new PicoRouter(List.of(path.split("/")));
  }

  @Override
  public boolean test(String path) {
    List<Tuple2<String, String>> zipped = parts.zip(PicoRouter.of(path).parts);
    return zipped.length() == parts.length()
        && zipped.map(t -> isDynamic(t) || isEqual(t)).fold(true, Boolean::logicalAnd);
  }

  @Override
  public Map<String, String> apply(String path) {
    return parts.zip(PicoRouter.of(path).parts)
        .filter(this::isDynamic)
        .map(t -> t.<String, String>map(l -> l.substring(1), r -> r))
        .toJavaMap(Function.identity());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PicoRouter that = (PicoRouter) o;
    return Objects.equals(parts, that.parts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parts);
  }

  @Override
  public String toString() {
    return parts.join("/");
  }

  private boolean isDynamic(Tuple2<String, String> t) {
    return t._1.startsWith(":");
  }

  private boolean isEqual(Tuple2<String, String> t) {
    return t._1.equals(t._2);
  }
}
