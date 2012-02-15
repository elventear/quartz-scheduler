/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.Scheduler;

import java.util.concurrent.CyclicBarrier;

public class Client2 extends ClientBase {

  public static final CyclicBarrier localBarrier = new CyclicBarrier(2);

  public static void main(String[] args) {
    new Client2(args).run();
  }

  public Client2(String args[]) {
    super(args);
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    // job should be recovered and trip barrier
    localBarrier.await();
  }
}