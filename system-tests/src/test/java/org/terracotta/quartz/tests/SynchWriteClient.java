package org.terracotta.quartz.tests;

import org.quartz.Scheduler;

public class SynchWriteClient extends ClientBase {

  public SynchWriteClient(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new SynchWriteClient(args).run();
  }

  @Override
  protected boolean isSynchWrite() {
    return true;
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    // does nothing really, test will inspect logging output
  }
}