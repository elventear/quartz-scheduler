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

package org.quartz.examples.prototype;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.cronScheduleDaily;
import static org.quartz.CronScheduleBuilder.cronScheduleDailyMonthly;
import static org.quartz.DateBuilder.dateOf;
import static org.quartz.DateBuilder.evenMinuteDateAfterNow;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.*;

import java.util.TimeZone;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrototypeExample {

    
    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(PrototypeExample.class);

        log.info("------- Initializing ----------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete -----------");

        log.info("------- Scheduling Jobs -------------------");


        // define the job and tie it to our HelloJob class
        JobDetailImpl job = new JobDetailImpl("job1", "group1", HelloJob.class);
        
        JobDetailImpl job2 = new JobDetailImpl("job2", "group2", HelloJob.class);
        
        
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        //   Cool prototype example starts here --
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~

        Trigger t = null;
        
        t = newTrigger()  // make a trigger with no specified name or schedule (get random UID name, and fire once now)
            .build();
        sched.scheduleJob(job, t);
            
        
        t = newTrigger()
            .withIdentity("myTriggerName")
            .withStartTimeNow()
            .withSchedule(simpleSchedule()
                            .repeatForever()
                            .withIntervalInSeconds(5))
            .forJob(job)                            
            .build();
        sched.scheduleJob(t);
        
        
        t = newTrigger()
            .withIdentity(triggerKey("myTriggerName2"))
            .withStartTime(futureDate(1, IntervalUnit.MINUTE))
            .withSchedule(simpleSchedule()
                            .repeatForever()
                            .withIntervalInMinutes(1))
            .build();
        sched.scheduleJob(job2, t);
        
        t = newTrigger()
            .withIdentity(triggerKey("myTriggerName3", "myGroup"))
            .withStartTime(dateOf(17, 15, 23)) // 17:15:23
            .withSchedule(simpleSchedule()
                            .withRepeatCount(5)
                            .withIntervalInMilliseconds(2000L)
                            .withMisfireHandlingInstructionNextWithExistingCount())
            .forJob("job1", "group1")                            
            .build();
        sched.scheduleJob(t);
        
        t = newTrigger()
            .withIdentity(triggerKey("myTriggerName4", "myGroup"))
            .withStartTime(evenMinuteDateAfterNow()) 
            .withSchedule(cronScheduleDaily(5, 30))  // every day at 5:30 am
            .forJob(job)                            
            .build();
        sched.scheduleJob(t);
        
        t = newTrigger()
            .withIdentity("myTriggerName5", "myGroup")
            .withSchedule(cronScheduleDailyMonthly(10, 5, 30) // on the 10th of every month at 5:30 am, in u.s. eastern time
                            .inTimeZone(TimeZone.getTimeZone("America/New_York"))
                            .withMisfireHandlingInstructionFireAndProceed()) 
            .forJob(job)                            
            .build();
        sched.scheduleJob(t);
        
        t = newTrigger()
            .withIdentity("myTriggerName6", "myGroup")
            .withSchedule(cronSchedule("0/2 * 7-18 ? * FRI")) // every 2 seconds between 7 am and 6 pm on Fridays   
            .forJob(job)                            
            .build();
        sched.scheduleJob(t);
        
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        //   Cool prototype example ends here --
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~   
        // * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~
        
            
            
            
            

        // Start up the scheduler (nothing can actually run until the 
        // scheduler has been started)
        sched.start();
        log.info("------- Started Scheduler -----------------");

        // wait long enough so that the scheduler as an opportunity to 
        // run the job!
        log.info("------- Waiting 90 seconds... -------------");
        try {
            // wait 90 seconds to show jobs
            Thread.sleep(90L * 1000L); 
            // executing...
        } catch (Exception e) {
        }

        // shut down the scheduler
        log.info("------- Shutting Down ---------------------");
        sched.shutdown(true);
        log.info("------- Shutdown Complete -----------------");
    }

    public static void main(String[] args) throws Exception {

        PrototypeExample example = new PrototypeExample();
        example.run();

    }

}
