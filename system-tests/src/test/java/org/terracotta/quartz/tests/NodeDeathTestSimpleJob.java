package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class NodeDeathTestSimpleJob implements Job {

  public void execute(JobExecutionContext context) {
    System.err.println("running " + getClass().getName());

    NodeDeathTestClient1.run.add(context.getJobDetail().getKey().getName());
  }
}