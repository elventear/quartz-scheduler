/*
 * Copyright (c) 2005 by OpenSymphony
 * All rights reserved.
 * 
 */
package org.quartz.examples.example11;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * This is just a simple job that gets fired off many times by example 1
 * </p>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

	private static Log _log = LogFactory.getLog(SimpleJob.class);

	// job parameter
	public static final String DELAY_TIME = "delay time";
	
	/**
	 * Empty constructor for job initilization
	 */
	public SimpleJob() {
	}

	/**
	 * <p>
	 * Called by the <code>{@link org.quartz.Scheduler}</code> when a
	 * <code>{@link org.quartz.Trigger}</code> fires that is associated with
	 * the <code>Job</code>.
	 * </p>
	 * 
	 * @throws JobExecutionException
	 *             if there is an exception while executing the job.
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		// This job simply prints out its job name and the
		// date and time that it is running
		String jobName = context.getJobDetail().getFullName();
		_log.info("Executing job: " + jobName + " executing at " + new Date());
		
        // wait for a period of time
		long delayTime = 
			context.getJobDetail().getJobDataMap().getLong(DELAY_TIME);
        try {
            Thread.sleep(delayTime); 
        } 
        catch (Exception e) {
        }		

        _log.info("Finished Executing job: " + jobName + " at " + new Date());
	}

}