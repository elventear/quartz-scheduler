package org.terracotta.quartz.tests;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.List;

public class PausedTriggerClient2 extends ClientBase {

  public PausedTriggerClient2(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new PausedTriggerClient2(args).run();
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey("job1", "group"));
    if (triggers.size() != 1) { throw new AssertionError(triggers); }
  }
}