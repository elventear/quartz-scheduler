/*
 * Copyright (c) 2005 by OpenSymphony
 * All rights reserved.
 * 
 */
package org.quartz.examples.example1;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.helpers.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This Example will demonstrate how to start and shutdown the Quartz 
 * scheduler and how to schedule a job to run in Quartz.
 * 
 * @author Bill Kratzer
 */
public class SimpleExample {

	
	public void run() throws Exception {
		Log log = LogFactory.getLog(SimpleExample.class);

		log.info("------- Initializing ----------------------");

		// First we must get a reference to a scheduler
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();

		log.info("------- Initialization Complete -----------");

		log.info("------- Scheduling Jobs -------------------");

		// computer a time that is on the next round minute
		Date runTime = TriggerUtils.getEvenMinuteDate(new Date());

		// define the job and tie it to our HelloJob class
		JobDetail job = new JobDetail("job1", "group1", HelloJob.class);
		
		// Trigger the job to run on the next round minute
		SimpleTrigger trigger = 
			new SimpleTrigger("trigger1", "group1", runTime);
		
		// Tell quartz to schedule the job using our trigger
		sched.scheduleJob(job, trigger);
		log.info(job.getFullName() + " will run at: " + runTime);  

		// Start up the scheduler (nothing can actually run until the 
		// scheduler has been started)
		sched.start();
		log.info("------- Started Scheduler -----------------");

		// wait long enough so that the scheduler as an opportunity to 
		// run the job!
		log.info("------- Waiting 90 seconds... -------------");
		try {
			// wait 90 seconds to show jobs
			Thread.sleep(90L * 1000L); 
			// executing...
		} catch (Exception e) {
		}

		// shut down the scheduler
		log.info("------- Shutting Down ---------------------");
		sched.shutdown(true);
		log.info("------- Shutdown Complete -----------------");
	}

	public static void main(String[] args) throws Exception {

		SimpleExample example = new SimpleExample();
		example.run();

	}

}