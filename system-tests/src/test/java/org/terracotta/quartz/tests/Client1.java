/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.concurrent.CyclicBarrier;

public class Client1 extends ClientBase {

  public static final CyclicBarrier localBarrier = new CyclicBarrier(2);

  public static void main(String[] args) {
    new Client1(args).run();
  }

  public Client1(String args[]) {
    super(args);
  }

  @Override
  protected boolean isSynchWrite() {
    return true;
  }

  protected void test(Scheduler scheduler) throws Throwable {
    JobDetailImpl jobDetail = new JobDetailImpl("recoveryjob", null, RecoveryTestJob.class);
    jobDetail.setDurability(true);
    jobDetail.setRequestsRecovery(true);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl("trigger1", "group");
    trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    trigger.setRepeatInterval(1000L * 60L * 60L * 24L * 7L);
    trigger.setJobName("recoveryjob");

    scheduler.addJob(jobDetail, false);
    scheduler.scheduleJob(trigger);

    localBarrier.await();
  }
}
