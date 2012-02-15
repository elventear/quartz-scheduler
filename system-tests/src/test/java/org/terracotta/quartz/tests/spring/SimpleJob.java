/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests.spring;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.TimeUnit;

public class SimpleJob implements Job {

  public void execute(JobExecutionContext context) throws JobExecutionException {
    System.err.println("Hi there");
    try {
      SimpleSpringClient1.localBarrier.await(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

}
