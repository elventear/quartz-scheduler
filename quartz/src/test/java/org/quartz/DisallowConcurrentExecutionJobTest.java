/* 
 * Copyright 2001-2011 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */
package org.quartz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobListenerSupport;

/**
 * Integration test for using DisallowConcurrentExecution annot.
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 */
public class DisallowConcurrentExecutionJobTest extends TestCase {
	
	private static final long JOB_BLOCK_TIME = 300L;
	
	public static List<Date> jobExecDates = Collections.synchronizedList(new ArrayList<Date>());
	
	public static final CyclicBarrier barrier = new CyclicBarrier(2);
	
	@DisallowConcurrentExecution
	public static class TestJob implements Job {
		public void execute(JobExecutionContext context) throws JobExecutionException {
			jobExecDates.add(new Date());
			try {
				Thread.sleep(JOB_BLOCK_TIME);
			} catch (InterruptedException e) {
				throw new JobExecutionException("Failed to pause job for testing.");
			}
		}
	}
	
	public static class TestJobListener extends JobListenerSupport {

		private final AtomicInteger jobExCount = new AtomicInteger(0);
		private final int jobExecutionCountToSyncAfter;
		
		public TestJobListener(int jobExecutionCountToSyncAfter) {
			this.jobExecutionCountToSyncAfter = jobExecutionCountToSyncAfter;
		}
		
		public String getName() {
			return "TestJobListener";
		}

		@Override
		public void jobWasExecuted(JobExecutionContext context,
				JobExecutionException jobException) {
			if(jobExCount.incrementAndGet() == jobExecutionCountToSyncAfter)
				try {
					barrier.await();
				} catch (Throwable e) {
					e.printStackTrace();
					throw new AssertionError("Await on barrier was interrupted: " + e.toString());
				} 
		}
	}
	
	@Override
	public void setUp() {
		jobExecDates.clear();
	}
	
	public void testNoConcurrentExecOnSameJob() throws Exception {
		Date startTime = new Date(System.currentTimeMillis() + 100); // make the triggers fire at the same time.
		
		JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
		Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).build();

		Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).forJob(job1.getKey()).build();

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.idleWaitTime", "1500");
		props.setProperty("org.quartz.threadPool.threadCount", "2");
		Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
		scheduler.getListenerManager().addJobListener(new TestJobListener(2));
		scheduler.scheduleJob(job1, trigger1);
		scheduler.scheduleJob(trigger2);
		scheduler.start();
		
		barrier.await(); // wait for jobs to execute...
		
		scheduler.shutdown(true);
		
		Assert.assertEquals(2, jobExecDates.size());
		Assert.assertTrue(jobExecDates.get(1).getTime() - jobExecDates.get(0).getTime() >= JOB_BLOCK_TIME);
	}
	
	/** QTZ-202 */
	public void testNoConcurrentExecOnSameJobWithBatching() throws Exception {
		Date startTime = new Date(System.currentTimeMillis() + 100); // make the triggers fire at the same time.
		
		JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
		Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).build();

		Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).forJob(job1.getKey()).build();

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.idleWaitTime", "1500");
		props.setProperty("org.quartz.scheduler.batchTriggerAcquisitionMaxCount", "2");
		props.setProperty("org.quartz.threadPool.threadCount", "2");
		Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
		scheduler.getListenerManager().addJobListener(new TestJobListener(2));
		scheduler.scheduleJob(job1, trigger1);
		scheduler.scheduleJob(trigger2);
		scheduler.start();
		
		barrier.await();
		
		scheduler.shutdown(true);
		
		Assert.assertEquals(2, jobExecDates.size());
		Assert.assertTrue(jobExecDates.get(1).getTime() - jobExecDates.get(0).getTime() >= JOB_BLOCK_TIME);
	}
}
