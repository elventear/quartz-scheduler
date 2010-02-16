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

package org.quartz.examples.example3;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This Example will demonstrate all of the basics of scheduling capabilities of
 * Quartz using Cron Triggers.
 * 
 * @author Bill Kratzer
 */
public class CronTriggerExample {


    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(CronTriggerExample.class);

        log.info("------- Initializing -------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete --------");

        log.info("------- Scheduling Jobs ----------------");

        // jobs can be scheduled before sched.start() has been called

        // job 1 will run every 20 seconds
        JobDetail job = new JobDetail("job1", "group1", SimpleJob.class);
        CronTrigger trigger = new CronTrigger("trigger1", "group1", "job1",
                "group1", "0/20 * * * * ?");
        sched.addJob(job, true);
        Date ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 2 will run every other minute (at 15 seconds past the minute)
        job = new JobDetail("job2", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger2", "group1", "job2", "group1",
                "15 0/2 * * * ?");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 3 will run every other minute but only between 8am and 5pm
        job = new JobDetail("job3", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger3", "group1", "job3", "group1",
                "0 0/2 8-17 * * ?");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 4 will run every three minutes but only between 5pm and 11pm
        job = new JobDetail("job4", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger4", "group1", "job4", "group1",
                "0 0/3 17-23 * * ?");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 5 will run at 10am on the 1st and 15th days of the month
        job = new JobDetail("job5", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger5", "group1", "job5", "group1",
                "0 0 10am 1,15 * ?");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 6 will run every 30 seconds but only on Weekdays (Monday through
        // Friday)
        job = new JobDetail("job6", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger6", "group1", "job6", "group1",
                "0,30 * * ? * MON-FRI");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        // job 7 will run every 30 seconds but only on Weekends (Saturday and
        // Sunday)
        job = new JobDetail("job7", "group1", SimpleJob.class);
        trigger = new CronTrigger("trigger7", "group1", "job7", "group1",
                "0,30 * * ? * SAT,SUN");
        sched.addJob(job, true);
        ft = sched.scheduleJob(trigger);
        log.info(job.getFullName() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        log.info("------- Starting Scheduler ----------------");

        // All of the jobs have been added to the scheduler, but none of the
        // jobs
        // will run until the scheduler has been started
        sched.start();

        log.info("------- Started Scheduler -----------------");

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

        SchedulerMetaData metaData = sched.getMetaData();
        log.info("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");

    }

    public static void main(String[] args) throws Exception {

        CronTriggerExample example = new CronTriggerExample();
        example.run();
    }

}
