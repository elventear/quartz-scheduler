/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples.example13;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * A dumb implementation of Job, for unittesting purposes.
 * </p>
 * 
 * @author James House
 */
public class SimpleRecoveryJob implements Job {

	private static Log _log = LogFactory.getLog(SimpleRecoveryJob.class);

	private static final String COUNT = "count";
	
	/**
	 * Quartz requires a public empty constructor so that the
	 * scheduler can instantiate the class whenever it needs.
	 */
	public SimpleRecoveryJob() {
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

		String jobName = context.getJobDetail().getFullName();

		// if the job is recovering print a message
        if (context.isRecovering()) {
            _log.info("SimpleRecoveryJob: " + jobName + " RECOVERING at " + new Date());
        }
        else {
            _log.info("SimpleRecoveryJob: " + jobName + " starting at " + new Date());
        }

        // delay for ten seconds
        long delay = 10L * 1000L;
        try {
        	Thread.sleep(delay);
        }
        catch (Exception e) {
        }

        JobDataMap data = context.getJobDetail().getJobDataMap();
        int count;
        if (data.containsKey(COUNT)) {
        	count = data.getInt(COUNT);
        }
        else {
        	count = 0;
        }
        count++;
        data.put(COUNT, count);
        
        _log.info("SimpleRecoveryJob: " + jobName + 
        		" done at " + new Date() + 
        		"\n Execution #" + count);
         
	}

	

}