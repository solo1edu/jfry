package com.github.ggalmazor.jfry;

import javaslang.control.Try;

interface LifeCycle<T> extends Runnable {
  Try<T> start();

  Try<T> stop();

  @Override
  default void run() {
    start();
  }
}
