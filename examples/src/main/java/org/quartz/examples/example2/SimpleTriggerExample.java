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

package org.quartz.examples.example2;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This Example will demonstrate all of the basics of scheduling capabilities
 * of Quartz using Simple Triggers.
 * 
 * @author Bill Kratzer
 */
public class SimpleTriggerExample {

    
    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(SimpleTriggerExample.class);

        log.info("------- Initializing -------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete --------");

        log.info("------- Scheduling Jobs ----------------");

        // jobs can be scheduled before sched.start() has been called

        // get a "nice round" time a few seconds in the future...
        long ts = TriggerUtils.getNextGivenSecondDate(null, 15).getTime();

        // job1 will only fire once at date/time "ts"
        JobDetail job = new JobDetail("job1", "group1", SimpleJob.class);
        SimpleTrigger trigger = 
            new SimpleTrigger("trigger1", "group1", new Date(ts));

        // schedule it to run!
        Date ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        // job2 will only fire once at date/time "ts"
        job = new JobDetail("job2", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger2", "group1", "job2", "group1",
                new Date(ts), null, 0, 0);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        // job3 will run 11 times (run once and repeat 10 more times)
        // job3 will repeat every 10 seconds (10000 ms)
        job = new JobDetail("job3", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger3", "group1", "job3", "group1",
                new Date(ts), null, 10, 10000L);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
        
        // the same job (job3) will be scheduled by a another trigger
        // this time will only run every 70 seocnds (70000 ms)
        trigger = new SimpleTrigger("trigger3", "group2", "job3", "group1",
                new Date(ts), null, 2, 70000L);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() +
                " will [also] run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        // job4 will run 6 times (run once and repeat 5 more times)
        // job4 will repeat every 10 seconds (10000 ms)
        job = new JobDetail("job4", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger4", "group1", "job4", "group1",
                new Date(ts), null, 5, 10000L);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        // job5 will run once, five minutes past "ts" (300 seconds past "ts")
        job = new JobDetail("job5", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger5", "group1", "job5", "group1",
                new Date(ts + 300000L), null, 0, 0);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        // job6 will run indefinitely, every 50 seconds
        job = new JobDetail("job6", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger6", "group1", "job6", "group1",
                new Date(ts), null, SimpleTrigger.REPEAT_INDEFINITELY, 50000L);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");

        log.info("------- Starting Scheduler ----------------");

        // All of the jobs have been added to the scheduler, but none of the jobs
        // will run until the scheduler has been started
        sched.start();

        log.info("------- Started Scheduler -----------------");

        // jobs can also be scheduled after start() has been called...
        // job7 will repeat 20 times, repeat every five minutes
        job = new JobDetail("job7", "group1", SimpleJob.class);
        trigger = new SimpleTrigger("trigger7", "group1", "job7", "group1",
                new Date(ts), null, 20, 300000L);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() +
                " will run at: " + ft +  
                " and repeat: " + trigger.getRepeatCount() + 
                " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
        
        // jobs can be fired directly... (rather than waiting for a trigger)
        job = new JobDetail("job8", "group1", SimpleJob.class);
        job.setDurability(true);
        sched.addJob(job, true);
        log.info("'Manually' triggering job8...");
        sched.triggerJob("job8", "group1");

        log.info("------- Waiting 30 seconds... --------------");

        try {
            // wait 30 seconds to show jobs
            Thread.sleep(30L * 1000L); 
            // executing...
        } catch (Exception e) {
        }

        // jobs can be re-scheduled...  
        // job 7 will run immediately and repeat 10 times for every second
        log.info("------- Rescheduling... --------------------");
        trigger = new SimpleTrigger("trigger7", "group1", "job7", "group1", 
                new Date(), null, 10, 1000L);
        ft = sched.rescheduleJob("trigger7", "group1", trigger);
        log.info("job7 rescheduled to run at: " + ft);
        
        log.info("------- Waiting five minutes... ------------");
        try {
            // wait five minutes to show jobs
            Thread.sleep(300L * 1000L); 
            // executing...
        } catch (Exception e) {
        }

        log.info("------- Shutting Down ---------------------");

        sched.shutdown(true);

        log.info("------- Shutdown Complete -----------------");

        // display some stats about the schedule that just ran
        SchedulerMetaData metaData = sched.getMetaData();
        log.info("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");

    }

    public static void main(String[] args) throws Exception {

        SimpleTriggerExample example = new SimpleTriggerExample();
        example.run();

    }

}
