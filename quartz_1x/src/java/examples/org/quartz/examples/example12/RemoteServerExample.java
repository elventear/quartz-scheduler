/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples.example12;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Bill Kratzer
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RemoteServerExample {

	/**
	 * This example will spawn a large number of jobs to run
	 * 
	 * @author James House, Bill Kratzer
	 */
	public void run() throws Exception {
		Log log = LogFactory.getLog(RemoteServerExample.class);

		// First we must get a reference to a scheduler
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();

		log.info("------- Initialization Complete -----------");

		log.info("------- (Not Scheduling any Jobs - relying on a remote client to schedule jobs --");

		log.info("------- Starting Scheduler ----------------");

		// start the schedule
		sched.start();

		log.info("------- Started Scheduler -----------------");

		log.info("------- Waiting ten minutes... ------------");

		// wait five minutes to give our jobs a chance to run
		try {
			Thread.sleep(600L * 1000L);
		} catch (Exception e) {
		}

		// shut down the scheduler
		log.info("------- Shutting Down ---------------------");
		sched.shutdown(true);
		log.info("------- Shutdown Complete -----------------");

		SchedulerMetaData metaData = sched.getMetaData();
		log.info("Executed " + metaData.numJobsExecuted() + " jobs.");
	}

	public static void main(String[] args) throws Exception {

		RemoteServerExample example = new RemoteServerExample();
		example.run();
	}

}
