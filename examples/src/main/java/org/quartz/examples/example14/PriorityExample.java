/* 
 * Copyright 2006-2009 Terracotta, Inc. 
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
package org.quartz.examples.example14;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This Example will demonstrate how Triggers are ordered by priority.
 */
public class PriorityExample {
    
    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(PriorityExample.class);

        log.info("------- Initializing ----------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory(
                "org/quartz/examples/example14/quartz_priority.properties");
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete -----------");

        log.info("------- Scheduling Jobs -------------------");

        JobDetail job = new JobDetail("TriggerEchoJob", null, TriggerEchoJob.class);

        // All three triggers will fire their first time at the same time, 
        // ordered by their priority, and then repeat once, firing in a 
        // staggered order that therefore ignores priority.
        //
        // We should see the following firing order:
        // 1. Priority10Trigger15SecondRepeat
        // 2. Priority5Trigger10SecondRepeat
        // 3. PriorityNeg5Trigger5SecondRepeat
        // 4. PriorityNeg5Trigger5SecondRepeat
        // 5. Priority5Trigger10SecondRepeat
        // 6. Priority10Trigger15SecondRepeat
        
        // Calculate the start time of all triggers as 5 seconds from now
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.SECOND, 5);
        
        // First trigger has priority of 1, and will repeat after 5 seconds
        SimpleTrigger trigger1 = 
            new SimpleTrigger("PriorityNeg5Trigger5SecondRepeat", null, startTime.getTime(), null, 1, 5L * 1000L);
        trigger1.setPriority(1);
        trigger1.setJobName("TriggerEchoJob");

        // Second trigger has default priority of 5, and will repeat after 10 seconds
        SimpleTrigger trigger2 = 
            new SimpleTrigger("Priority5Trigger10SecondRepeat", null, startTime.getTime(), null, 1, 10L * 1000L);
        trigger2.setJobName("TriggerEchoJob");
        
        // Third trigger has priority 10, and will repeat after 15 seconds
        SimpleTrigger trigger3 = 
            new SimpleTrigger("Priority10Trigger15SecondRepeat", null, startTime.getTime(), null, 1, 15L * 1000L);
        trigger3.setPriority(10);
        trigger3.setJobName("TriggerEchoJob");
        
        // Tell quartz to schedule the job using our trigger
        sched.scheduleJob(job, trigger1);
        sched.scheduleJob(trigger2);
        sched.scheduleJob(trigger3);

        // Start up the scheduler (nothing can actually run until the 
        // scheduler has been started)
        sched.start();
        log.info("------- Started Scheduler -----------------");

        // wait long enough so that the scheduler as an opportunity to 
        // fire the triggers
        log.info("------- Waiting 30 seconds... -------------");
        try {
            Thread.sleep(30L * 1000L); 
            // executing...
        } catch (Exception e) {
        }

        // shut down the scheduler
        log.info("------- Shutting Down ---------------------");
        sched.shutdown(true);
        log.info("------- Shutdown Complete -----------------");
    }

    public static void main(String[] args) throws Exception {
        PriorityExample example = new PriorityExample();
        example.run();
    }
}
