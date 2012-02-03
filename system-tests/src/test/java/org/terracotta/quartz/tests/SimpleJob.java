package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class SimpleJob implements Job {
  public static final CyclicBarrier localBarrier = new CyclicBarrier(2);

  public void execute(JobExecutionContext context) throws JobExecutionException {
    System.err.println("Hi there");
    try {
      localBarrier.await(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}