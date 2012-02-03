/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.junit.Assert;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

public class ShutdownHookClient extends ClientBase {

  public static void main(String[] args) {
    new ShutdownHookClient(args).run();
  }

  public ShutdownHookClient(String[] args) {
    super(args);
  }

  @Override
  protected void test(final Scheduler scheduler) throws Throwable {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.err.println("Running shutdown hook");
        try {
          scheduler.shutdown(true);
          System.err.println("Scheduler shutdown completed normally - signalling success");
          pass();
        } catch (Throwable t) {
          System.err.println("Scheduler shutdown completed abnormally - halting JVM");
          t.printStackTrace();
          Runtime.getRuntime().halt(-1);
        }
      }
    });

    JobDetail jobDetail = new JobDetailImpl("job", ShutdownHookJob.class);
    Trigger trigger = new SimpleTriggerImpl("trigger", SimpleTrigger.REPEAT_INDEFINITELY, 10L);
    scheduler.scheduleJob(jobDetail, trigger);

    Thread.sleep(3000L);

    System.err.println("Starting VM shutdown, the shutdown hook will handle the test from here.");
    Runtime.getRuntime().exit(0);
    Assert.fail();
  }

  public static class ShutdownHookJob implements Job {

    public void execute(JobExecutionContext arg0) {
      //
    }

  }

}
