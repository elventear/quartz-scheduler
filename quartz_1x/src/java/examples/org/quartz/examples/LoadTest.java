/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

/**
 * @author James House
 */
public class LoadTest {

    public static void loadTest(SchedulerFactory sf) throws Exception {
        Log lg = LogFactory.getLog(LoadTest.class);

        lg.info("------- Initializing -------------------");

        Scheduler sched = sf.getScheduler();

        lg.warn("      *** Deleting existing jobs/triggers ***");

        String[] groups = sched.getTriggerGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = sched.getTriggerNames(groups[i]);
            for (int j = 0; j < names.length; j++)
                sched.unscheduleJob(names[j], groups[i]);
        }
        groups = sched.getJobGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = sched.getJobNames(groups[i]);
            for (int j = 0; j < names.length; j++)
                sched.deleteJob(names[j], groups[i]);
        }

        lg.info("------- Initialization Complete -----------");

        lg.info("------- Scheduling a Big Pile of Jobs -----------");

        String schedId = sched.getSchedulerInstanceId();

        int count = 1;
        for (; count <= 500; count++) {
            JobDetail job = new JobDetail("job_" + count, "grp_1",
                    DumbDelayJob.class);
            job.getJobDataMap().put(DumbDelayJob.KEY_DELAY, 60000L);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            SimpleTrigger trigger = new SimpleTrigger("trig_" + count, "grp_1");
            trigger.setStartTime(new Date(System.currentTimeMillis() + 10000L
                    + (count * 100)));
            //lg.info(job.getFullName() + " will run at: " +
            // trigger.getNextFireTime() + " & repeat: " +
            // trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);
            if (count % 25 == 0) lg.info("...scheduled " + count + " jobs...");
        }
        lg.info("Finished scheduling " + count + " jobs.");

        lg.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...

        sched.start();
        lg.info("------- Started Scheduler -----------------");

        lg.info("------- Waiting... -----------------------");

        try {
            Thread.sleep(600L * 1000L);
        } catch (Exception e) {
        }

        lg.info("------- Shutting Down ---------------------");

        sched.shutdown();

        lg.info("------- Shutdown Complete -----------------");
    }

    public static void main(String[] args) throws Exception {
        try {

            // Configure Log4J
            // org.apache.log4j.PropertyConfigurator.configure(System.getProperty("log4jConfigFile",
            // "log4j.properties"));

            boolean clearJobs = false;
            boolean scheduleJobs = true;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("clearJobs")) clearJobs = true;
                if (args[i].equals("dontScheduleJobs")) scheduleJobs = false;
            }

            loadTest(new org.quartz.impl.StdSchedulerFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// EOF
