/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

/**
 * @author James House
 */
public class QuartzRemoteClient {

    QuartzRemoteClient() {
    }

    public static void useRemoteScheduler(SchedulerFactory schedFact)
            throws Exception {

        Scheduler sched = schedFact.getScheduler();

        JobDetail job = new JobDetail("remotelyAddedJob", "default",
                DumbJob2.class);
        JobDataMap map = new JobDataMap();
        map.put("msg", "Your remotely added job has executed!");
        job.setJobDataMap(map);
        //    SimpleTrigger st = new SimpleTrigger("remotelyAddedTrigger",
        // "default", "remotelyAddedJob", "default", new Date(), null, 0, 0);
        CronTrigger ct = new CronTrigger("remotelyAddedTrigger", "default",
                "remotelyAddedJob", "default", new Date(), null, "/5 * * ? * *");

        sched.scheduleJob(job, ct);

        System.err.println("\n*** Remote job scheduled.");

    }

    public static void main(String[] args) throws Exception {

        // Configure Log4J
        // org.apache.log4j.PropertyConfigurator.configure(System.getProperty("log4jConfigFile",
        // "log4j.properties"));

        try {
            useRemoteScheduler(new org.quartz.impl.StdSchedulerFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}