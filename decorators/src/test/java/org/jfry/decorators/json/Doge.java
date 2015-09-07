package org.jfry.decorators.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

class Doge {
  @JsonProperty
  private final String name;
  @JsonProperty
  private final String sound;

  @JsonCreator
  public Doge(@JsonProperty(value = "name") String name, @JsonProperty(value = "sound") String sound) {
    this.name = name;
    this.sound = sound;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Doge doge = (Doge) o;
    return Objects.equals(name, doge.name) &&
        Objects.equals(sound, doge.sound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, sound);
  }

  @Override
  public String toString() {
    return "Doge{" +
        "name='" + name + '\'' +
        ", sound='" + sound + '\'' +
        '}';
  }
}