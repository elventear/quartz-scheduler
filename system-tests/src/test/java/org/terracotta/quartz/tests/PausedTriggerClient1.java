package org.terracotta.quartz.tests;

import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.jobs.NoOpJob;

public class PausedTriggerClient1 extends ClientBase {

  public PausedTriggerClient1(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new PausedTriggerClient1(args).run();
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    String pausedGroup = "group";

    scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals(pausedGroup));

    JobDetailImpl jobDetail = new JobDetailImpl("job1", "group", NoOpJob.class);
    jobDetail.setDurability(true);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl("trigger1", pausedGroup);
    trigger.setRepeatInterval(30000);
    trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    trigger.setJobName("job1");
    trigger.setJobGroup("group");

    scheduler.addJob(jobDetail, false);
    scheduler.scheduleJob(trigger);
  }
}
