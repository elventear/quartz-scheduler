/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

public class RecoveryTestJob implements Job {

  public void execute(JobExecutionContext context) throws JobExecutionException {
    System.err.println("Hi There");

    if (context.getMergedJobDataMap().getString(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_NAME) == null) {
      // If not recovering, just exit the VM
      try {
        Client1.localBarrier.await();
      } catch (Exception e) {
        throw new JobExecutionException(e);
      }

      System.exit(0);
    } else {
      try {
        Client2.localBarrier.await();
      } catch (Exception e) {
        throw new JobExecutionException(e);
      }
    }
  }

}