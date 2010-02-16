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

package org.quartz.examples.example6;

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
 * 
 * This job demonstrates how Quartz can handle JobExecutionExceptions that are
 * thrown by jobs.
 * 
 * @author Bill Kratzer
 */
public class JobExceptionExample {

    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(JobExceptionExample.class);

        log.info("------- Initializing ----------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete ------------");

        log.info("------- Scheduling Jobs -------------------");

        // jobs can be scheduled before start() has been called

        // get a "nice round" time a few seconds in the future...
        long ts = TriggerUtils.getNextGivenSecondDate(null, 15).getTime();

        // badJob1 will run every three seconds
        // this job will throw an exception and refire
        // immediately
        JobDetail job = new JobDetail("badJob1", "group1", BadJob1.class);
        SimpleTrigger trigger = new SimpleTrigger("trigger1", "group1",
                new Date(ts), null, SimpleTrigger.REPEAT_INDEFINITELY, 3000L);
        Date ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() + " will run at: " + ft + " and repeat: "
                + trigger.getRepeatCount() + " times, every "
                + trigger.getRepeatInterval() / 1000 + " seconds");

        // badJob2 will run every three seconds
        // this job will throw an exception and never
        // refire
        job = new JobDetail("badJob2", "group1", BadJob2.class);
        trigger = new SimpleTrigger("trigger2", "group1", new Date(ts), null,
                SimpleTrigger.REPEAT_INDEFINITELY, 3000L);
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getFullName() + " will run at: " + ft + " and repeat: "
                + trigger.getRepeatCount() + " times, every "
                + trigger.getRepeatInterval() / 1000 + " seconds");

        log.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...
        sched.start();

        log.info("------- Started Scheduler -----------------");

        try {
            // sleep for 60 seconds
            Thread.sleep(60L * 1000L);
        } catch (Exception e) {
        }

        log.info("------- Shutting Down ---------------------");

        sched.shutdown(true);

        log.info("------- Shutdown Complete -----------------");

        SchedulerMetaData metaData = sched.getMetaData();
        log.info("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");
    }

    public static void main(String[] args) throws Exception {

        JobExceptionExample example = new JobExceptionExample();
        example.run();
    }

}
