package com.github.ggalmazor.jfry;

interface LifeCycle<T> extends Runnable {
  T start();

  T stop();

  @Override
  default void run() {
    start();
  }
}
