/*
 * Copyright 2001-2009 Terracotta, Inc.
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

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForTotalCount;
import static org.quartz.TriggerBuilder.newTrigger;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A unit test to reproduce QTZ-205 bug:
 * A TriggerListener vetoed job will affect SchedulerListener's triggerFinalized() notification. 
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 */
public class Qtz205SchedulerListenerTest extends TestCase {
	private static Logger logger = LoggerFactory.getLogger(Qtz205SchedulerListenerTest.class);
	private static int jobExecutionCount = 0;	
	
	public static class Qtz205Job implements Job {
		public void execute(JobExecutionContext context) throws JobExecutionException {
			jobExecutionCount++;
			logger.info("Job executed. jobExecutionCount=" + jobExecutionCount);
		}
		
	}
	
	public static class Qtz205TriggerListener implements TriggerListener {
		private int fireCount;
		public int getFireCount() {
			return fireCount;
		}
		public String getName() {
			return "Qtz205TriggerListener";
		}

		public void triggerFired(Trigger trigger, JobExecutionContext context) {
			fireCount++;
			logger.info("Trigger fired. count " + fireCount);
		}

		public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
			if (fireCount >= 3) {
				return true;
			} else {
				return false;
			}
		}

		public void triggerMisfired(Trigger trigger) {
		}

		public void triggerComplete(Trigger trigger,
				JobExecutionContext context,
				CompletedExecutionInstruction triggerInstructionCode) {
		}
		
	}
	
	public static class Qtz205ScheListener implements SchedulerListener {
		private int triggerFinalizedCount;
		public int getTriggerFinalizedCount() {
			return triggerFinalizedCount;
		}
		public void jobScheduled(Trigger trigger) {
		}

		public void jobUnscheduled(TriggerKey triggerKey) {
		}

		public void triggerFinalized(Trigger trigger) {
			triggerFinalizedCount ++;
			logger.info("triggerFinalized " + trigger);
		}

		public void triggerPaused(TriggerKey triggerKey) {
		}

		public void triggersPaused(String triggerGroup) {	
		}

		public void triggerResumed(TriggerKey triggerKey) {
		}

		public void triggersResumed(String triggerGroup) {
		}

		public void jobAdded(JobDetail jobDetail) {
		}

		public void jobDeleted(JobKey jobKey) {
		}

		public void jobPaused(JobKey jobKey) {
		}

		public void jobsPaused(String jobGroup) {
		}

		public void jobResumed(JobKey jobKey) {
		}

		public void jobsResumed(String jobGroup) {
			
		}

		public void schedulerError(String msg, SchedulerException cause) {			
		}

		public void schedulerInStandbyMode() {
		}

		public void schedulerStarted() {
		}

		public void schedulerShutdown() {
		}

		public void schedulerShuttingdown() {
		}

		public void schedulingDataCleared() {
		}
	}
	
	/** QTZ-205 */
	public void testTriggerFinalized() throws Exception {
		Qtz205TriggerListener triggerListener = new Qtz205TriggerListener();
		Qtz205ScheListener schedulerListener = new Qtz205ScheListener();
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.getListenerManager().addSchedulerListener(schedulerListener);
		scheduler.getListenerManager().addTriggerListener(triggerListener);
		
		JobDetail job = newJob(Qtz205Job.class).withIdentity("test").build();
		Trigger trigger = newTrigger().withIdentity("test")
				.withSchedule(repeatSecondlyForTotalCount(3))
				.build();
		scheduler.scheduleJob(job, trigger);
		scheduler.start();
		Thread.sleep(4000);
		
		scheduler.shutdown(true);

		Assert.assertEquals(2, jobExecutionCount);
		Assert.assertEquals(3, triggerListener.getFireCount());
		Assert.assertEquals(1, schedulerListener.getTriggerFinalizedCount());
	}
}
