package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.JobDetailImpl;

public class NodeDeathTestCronJob implements Job {

  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDetailImpl jobDetail = ((JobDetailImpl) context.getJobDetail());
    System.err.println("running " + jobDetail.getFullName() + "...");
    try {
      Thread.sleep(3000L);
    } catch (InterruptedException ie) {
      throw new JobExecutionException(ie);
    }
    System.err.println("--> done running " + jobDetail.getFullName());

    NodeDeathTestClient1.run.add(jobDetail.getName());
  }
}
