/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class NullJob implements Job {

  public void execute(JobExecutionContext context) {
    System.err.println("Hi there from " + getClass());
  }

}
