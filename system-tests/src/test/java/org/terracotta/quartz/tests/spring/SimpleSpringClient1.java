package org.terracotta.quartz.tests.spring;

import org.quartz.Scheduler;

import java.util.concurrent.CyclicBarrier;

public class SimpleSpringClient1 extends SpringClientBase {

  public static final CyclicBarrier localBarrier = new CyclicBarrier(2);

  public SimpleSpringClient1(String[] args) {
    super("simple-spring-client1.xml", args);
  }

  public static void main(String[] args) {
    new SimpleSpringClient1(args).run();
  }

  @Override
  protected void test0(Scheduler scheduler) throws Throwable {
    localBarrier.await();
  }
}