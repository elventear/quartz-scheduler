package org.terracotta.quartz.tests;

import org.quartz.Scheduler;

public class BasicClient2 extends ClientBase {

  public BasicClient2(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new BasicClient2(args).run();
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    SimpleJob.localBarrier.await();
  }

}