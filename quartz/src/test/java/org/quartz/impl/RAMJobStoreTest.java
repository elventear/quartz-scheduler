package org.quartz.impl;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.jobs.NoOpJob;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;

public class RAMJobStoreTest extends TestCase {
	
	public void testStoreAndRetrieveJobs() throws Exception {
		RAMJobStore store = new RAMJobStore();
		
		// Store jobs.
		for (int i=0; i < 10; i++) {
			JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
			store.storeJob(job, false);
		}
		// Retrieve jobs.
		for (int i=0; i < 10; i++) {
			JobKey jobKey = JobKey.jobKey("job" + i);
			JobDetail storedJob = store.retrieveJob(jobKey);
			Assert.assertEquals(jobKey, storedJob.getKey());
		}
	}
	
	public void testStoreAndRetriveTriggers() throws Exception {
		RAMJobStore store = new RAMJobStore();
		
		// Store jobs and triggers.
		for (int i=0; i < 10; i++) {
			JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
			store.storeJob(job, true);
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule();
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).build();
			store.storeTrigger((OperableTrigger)trigger, true);
		}
		// Retrieve job and trigger.
		for (int i=0; i < 10; i++) {
			JobKey jobKey = JobKey.jobKey("job" + i);
			JobDetail storedJob = store.retrieveJob(jobKey);
			Assert.assertEquals(jobKey, storedJob.getKey());
			
			TriggerKey triggerKey = TriggerKey.triggerKey("job" + i);
			Trigger storedTrigger = store.retrieveTrigger(triggerKey);
			Assert.assertEquals(triggerKey, storedTrigger.getKey());
		}
	}
	
	public static class DummySchedulerSignaler implements SchedulerSignaler {

		public void notifyTriggerListenersMisfired(Trigger trigger) {			
		}

		public void notifySchedulerListenersFinalized(Trigger trigger) {
		}

		public void notifySchedulerListenersJobDeleted(JobKey jobKey) {
		}

		public void signalSchedulingChange(long candidateNewNextFireTime) {
		}
		
	}
	public void testAcquireTriggers() throws Exception {
		SchedulerSignaler schedSignaler = new DummySchedulerSignaler();
		ClassLoadHelper loadHelper = new CascadingClassLoadHelper();
		loadHelper.initialize();
		
		RAMJobStore store = new RAMJobStore();
		store.initialize(loadHelper, schedSignaler);
		
		// Setup: Store jobs and triggers.
		long MIN = 60 * 1000L;
		Date startTime0 = new Date(System.currentTimeMillis() + MIN); // a min from now.
		for (int i=0; i < 10; i++) {
			Date startTime = new Date(startTime0.getTime() + i * MIN); // a min apart
			JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder.repeatMinutelyForever(2);
			OperableTrigger trigger = (OperableTrigger)TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).startAt(startTime).build();
			
			// Manually trigger the first fire time computation that scheduler would do. Otherwise 
			// the store.acquireNextTriggers() will not work properly.
	        Date fireTime = trigger.computeFirstFireTime(null);
	        Assert.assertEquals(true, fireTime != null);
			
			store.storeJobAndTrigger(job, trigger);
		}
		
		// Test acquire one trigger at a time
		for (int i=0; i < 10; i++) {
			long noLaterThan = startTime0.getTime() + i * MIN;
			int maxCount = 1;
			long timeWindow = 0;
			List<OperableTrigger> triggers = store.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
			Assert.assertEquals(1, triggers.size());
			Assert.assertEquals("job" + i, triggers.get(0).getKey().getName());
			
			// Let's remove the trigger now.
			store.removeJob(triggers.get(0).getJobKey());
		}
	}
	
	public void testAcquireTriggersInBatch() throws Exception {
		SchedulerSignaler schedSignaler = new DummySchedulerSignaler();
		ClassLoadHelper loadHelper = new CascadingClassLoadHelper();
		loadHelper.initialize();
		
		RAMJobStore store = new RAMJobStore();
		store.initialize(loadHelper, schedSignaler);
		
		// Setup: Store jobs and triggers.
		long MIN = 60 * 1000L;
		Date startTime0 = new Date(System.currentTimeMillis() + MIN); // a min from now.
		for (int i=0; i < 10; i++) {
			Date startTime = new Date(startTime0.getTime() + i * MIN); // a min apart
			JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder.repeatMinutelyForever(2);
			OperableTrigger trigger = (OperableTrigger)TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).startAt(startTime).build();
			
			// Manually trigger the first fire time computation that scheduler would do. Otherwise 
			// the store.acquireNextTriggers() will not work properly.
	        Date fireTime = trigger.computeFirstFireTime(null);
	        Assert.assertEquals(true, fireTime != null);
			
			store.storeJobAndTrigger(job, trigger);
		}
		
		// Test acquire batch of triggers at a time
		long noLaterThan = startTime0.getTime() + 10 * MIN;
		int maxCount = 7;
		long timeWindow = 0;
		List<OperableTrigger> triggers = store.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
		Assert.assertEquals(7, triggers.size());
		for (int i=0; i < 7; i++) {
			Assert.assertEquals("job" + i, triggers.get(i).getKey().getName());
		}
	}

}
