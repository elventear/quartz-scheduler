/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples.example12;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This example is a client program that will remotely 
 * talk to the scheduler to schedule a job.   In this 
 * example, we will need to use the JDBC Job Store.  The 
 * client will connect to the JDBC Job Store remotely to 
 * schedule the job.
 * 
 * @author James House, Bill Kratzer
 */
public class RemoteClientExample {

    public void run() throws Exception {

		Log log = LogFactory.getLog(RemoteClientExample.class);

		// First we must get a reference to a scheduler
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();

		// define the job and ask it to run
        JobDetail job = 
        	new JobDetail("remotelyAddedJob", "default", SimpleJob.class);
        JobDataMap map = new JobDataMap();
        map.put("msg", "Your remotely added job has executed!");
        job.setJobDataMap(map);
        CronTrigger trigger = new CronTrigger(
        		"remotelyAddedTrigger", "default",
                "remotelyAddedJob", "default", 
                new Date(), 
                null, 
                "/5 * * ? * *");

        // schedule the job
        sched.scheduleJob(job, trigger);

		log.info("Remote job scheduled.");
    }

    public static void main(String[] args) throws Exception {

		RemoteClientExample example = new RemoteClientExample();
		example.run();
    }

}