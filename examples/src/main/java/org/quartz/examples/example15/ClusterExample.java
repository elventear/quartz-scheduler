/* 
 * Copyright 2005 - 2009 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package org.quartz.examples.example15;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Used to test/show the clustering features of TC JobStore.
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
 * @author James House
 */
public class ClusterExample {

    private static Logger _log = LoggerFactory.getLogger(ClusterExample.class);

    public void cleanUp(Scheduler inScheduler) throws Exception {
        _log.warn("***** Deleting existing jobs/triggers *****");

        // unschedule jobs
        String[] groups = inScheduler.getTriggerGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = inScheduler.getTriggerNames(groups[i]);
            for (int j = 0; j < names.length; j++) {
                inScheduler.unscheduleJob(names[j], groups[i]);
            }
        }

        // delete jobs
        groups = inScheduler.getJobGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = inScheduler.getJobNames(groups[i]);
            for (int j = 0; j < names.length; j++) {
                inScheduler.deleteJob(names[j], groups[i]);
            }
        }
    }

    public void run(boolean inClearJobs, boolean inScheduleJobs)
        throws Exception {

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        if (inClearJobs) {
            cleanUp(sched);
        }

        _log.info("------- Initialization Complete -----------");

        if (inScheduleJobs) {

            _log.info("------- Scheduling Jobs ------------------");

            String schedId = sched.getSchedulerInstanceId();

            int count = 1;

            JobDetail job = new JobDetail("job_" + count, schedId,
                    SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            SimpleTrigger trigger =
                new SimpleTrigger("triger_" + count, schedId, 20, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +
                    " and repeat: " + trigger.getRepeatCount() +
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId,
                    SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 2000L));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +
                    " and repeat: " + trigger.getRepeatCount() +
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId,
                    SimpleRecoveryStatefulJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 3000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +
                    " and repeat: " + trigger.getRepeatCount() +
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 4000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            _log.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 20, 4500L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + 1000L));
            _log.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);
        }

        // jobs don't start firing until start() has been called...
        _log.info("------- Starting Scheduler ---------------");
        sched.start();
        _log.info("------- Started Scheduler ----------------");

        _log.info("------- Waiting for one hour... ----------");
        try {
            Thread.sleep(3600L * 1000L);
        } catch (Exception e) {
        }

        _log.info("------- Shutting Down --------------------");
        sched.shutdown();
        _log.info("------- Shutdown Complete ----------------");
    }

    public static void main(String[] args) throws Exception {
        boolean clearJobs = false;
        boolean scheduleJobs = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("clearJobs")) {
                clearJobs = true;
            } else if (args[i].equalsIgnoreCase("dontScheduleJobs")) {
                scheduleJobs = false;
            }
        }

        ClusterExample example = new ClusterExample();
        example.run(clearJobs, scheduleJobs);
    }
}