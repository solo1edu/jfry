package org.jfry;

import javaslang.control.Try;

interface LifeCycle<T> {
  Try<T> start();

  Try<T> stop();
}
