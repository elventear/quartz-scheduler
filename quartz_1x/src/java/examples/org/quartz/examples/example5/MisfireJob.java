/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples.example5;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.StatefulJob;
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
public class MisfireJob implements StatefulJob {

	// Logging
	private static Log _log = LogFactory.getLog(MisfireJob.class);

	// Constants
    public static final String NUM_EXECUTIONS = "NumExecutions";
    public static final String EXECUTION_DELAY = "ExecutionDelay";

	/**
	 * Empty public constructor for job initilization
	 */
    public MisfireJob() {
    }

    /**
     * <p>
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a <code>{@link org.quartz.Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     * 
     * @throws JobExecutionException
     *           if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
    	String jobName = context.getJobDetail().getFullName();
        _log.info("---" + jobName + " executing at " + new Date());

        // default delay to five seconds
        long delay = 5000L;

        // use the delay passed in as a job parameter (if it exists)
        JobDataMap map = context.getJobDetail().getJobDataMap();
        if (map.containsKey(EXECUTION_DELAY)) {
        	delay = map.getLong(EXECUTION_DELAY);
        }

        try {
            Thread.sleep(delay);
        } 
        catch (Exception ignore) {
        }

        _log.info("---" + jobName + " completed at " + new Date());
    }

}