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
 * Used to test/show the clustering features of JDBCJobStore (JobStoreTX or
 * JobStoreCMT).
 * 
 * <p>
 * All instances MUST use a different properties file, because their instance
 * Ids must be different, however all other properties should be the same.
 * </p>
 * 
 * <p>
 * If you want it to clear out existing jobs & triggers, pass a command-line
 * argument called "clearJobs".
 * </p>
 * 
 * <p>
 * You should probably start with a "fresh" set of tables (assuming you may
 * have some data lingering in it from other tests), since mixing data from a
 * non-clustered setup with a clustered one can be bad.
 * </p>
 * 
 * <p>
 * Try killing one of the cluster instances while they are running, and see
 * that the remaining instance(s) recover the in-progress jobs. Note that
 * detection of the failure may take up to 15 or so seconds with the default
 * settings.
 * </p>
 * 
 * <p>
 * Also try running it with/without the shutdown-hook plugin registered with
 * the scheduler. (org.quartz.plugins.management.ShutdownHookPlugin).
 * </p>
 * 
 * <p>
 * <i>Note:</i> Never run clustering on separate machines, unless their
 * clocks are synchronized using some form of time-sync service (daemon).
 * </p>
 * 
 * @see DumbRecoveryJob
 * 
 * @author James House
 */
public class ClusterTest {

    public static void clusterTest(SchedulerFactory sf, boolean clearJobs,
            boolean scheduleJobs) throws Exception {
        Log lg = LogFactory.getLog(ClusterTest.class);

        lg.info("------- Initializing -------------------");

        Scheduler sched = sf.getScheduler();

        if (clearJobs) {
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
        }

        lg.info("------- Initialization Complete -----------");

        if (scheduleJobs) {

            lg.info("------- Scheduling Jobs -----------");

            String schedId = sched.getSchedulerInstanceId();

            int count = 1;

            JobDetail job = new JobDetail("job_" + count, schedId,
                    DumbRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            SimpleTrigger trigger = new SimpleTrigger("trig_" + count, schedId,
                    20, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            lg.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, DumbRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 2000L));
            lg.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId,
                    DumbRecoveryStatefulJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 3000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            lg.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, DumbRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 4000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            lg.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, DumbRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 4500L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            lg.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);
        }

        lg.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...

        sched.start();

        lg.info("------- Started Scheduler -----------------");

        lg.info("------- Waiting... -----------------------");

        try {
            Thread.sleep(3600L * 1000L);
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

            clusterTest(new org.quartz.impl.StdSchedulerFactory(), clearJobs,
                    scheduleJobs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// EOF
