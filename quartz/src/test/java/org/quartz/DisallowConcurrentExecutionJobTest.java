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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.quartz.impl.StdSchedulerFactory;

/**
 * Integration test for using DisallowConcurrentExecution annot.
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 */
public class DisallowConcurrentExecutionJobTest extends TestCase {
	
	public static List<Date> jobExecDates = Collections.synchronizedList(new ArrayList<Date>());
	
	@DisallowConcurrentExecution
	public static class TestJob implements Job {
		public void execute(JobExecutionContext context) throws JobExecutionException {
			jobExecDates.add(new Date());
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				throw new JobExecutionException("Failed to pause job for testing.");
			}
		}
	}
	
	@Override
	public void setUp() {
		jobExecDates.clear();
	}
	
	public void testNoConcurrentExecOnSameJob() throws Exception {
		Date startTime = new Date(System.currentTimeMillis() + 300); // make the triggers fire at the same time.
		
		JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
		Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).build();

		Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).forJob(job1.getKey()).build();

		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.scheduleJob(job1, trigger1);
		scheduler.scheduleJob(trigger2);
		
		scheduler.start();
		Thread.sleep(1000);
		scheduler.shutdown(true);
		
		Assert.assertEquals(2, jobExecDates.size());
		Assert.assertEquals(true, jobExecDates.get(0).getTime() < jobExecDates.get(1).getTime());
	}
	
	/** QTZ-202 */
	public void testNoConcurrentExecOnSameJobWithBatching() throws Exception {
		Date startTime = new Date(System.currentTimeMillis() + 300); // make the triggers fire at the same time.
		
		JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
		Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).build();

		Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startTime).forJob(job1.getKey()).build();

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.batchTriggerAcquisitionMaxCount", "2");
		props.setProperty("org.quartz.threadPool.threadCount", "2");
		Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
		scheduler.scheduleJob(job1, trigger1);
		scheduler.scheduleJob(trigger2);
		
		scheduler.start();
		Thread.sleep(1000);
		scheduler.shutdown(true);
		
		Assert.assertEquals(2, jobExecDates.size());
		Assert.assertEquals(true, jobExecDates.get(0).getTime() < jobExecDates.get(1).getTime());
	}
}
