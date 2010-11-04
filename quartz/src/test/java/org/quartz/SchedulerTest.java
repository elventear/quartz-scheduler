/*
 * Copyright 2001-2009 Terracotta, Inc.
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
 */
package org.quartz;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerKey.*;

/**
 * Test High Level Scheduler functionality (implicitly tests the underlying jobstore (RAMJobStore))
 */
public class SchedulerTest extends TestCase {


    public static class TestStatefulJob implements StatefulJob {
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
        }
    }

    public static class TestJob implements Job {
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
        }
    }
    
    @DisallowConcurrentExecution
    @PersistJobDataAfterExecution
    public static class TestAnnotatedJob implements Job {
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
        }
    }
    
    @Override
    protected void setUp() throws Exception {
    }

    public void testBasicStorageFunctions() throws Exception {
        Properties config = new Properties();
        config.setProperty("org.quartz.threadPool.threadCount", "2");
        config.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        Scheduler sched = new StdSchedulerFactory(config).getScheduler();

        // test basic storage functions of scheduler...
        
        JobDetail job = newJob()
            .ofType(TestJob.class)
            .withIdentity("j1")
            .storeDurably()
            .build();

        assertFalse("Unexpected existence of job named 'j1'.", sched.checkExists(jobKey("j1")));

        sched.addJob(job, false); 

        assertTrue("Expected existence of job named 'j1' but checkExists return false.", sched.checkExists(jobKey("j1")));

        job = sched.getJobDetail(jobKey("j1"));

        assertNotNull("Stored job not found!", job);
        
        sched.deleteJob(jobKey("j1"));
        
        Trigger trigger = newTrigger()
            .withIdentity("t1")
            .forJob(job)
            .startNow()
            .withSchedule(simpleSchedule()
                    .repeatForever()
                    .withIntervalInSeconds(5))
             .build();

        assertFalse("Unexpected existence of trigger named '11'.", sched.checkExists(triggerKey("t1")));

        sched.scheduleJob(job, trigger);
        
        assertTrue("Expected existence of trigger named 't1' but checkExists return false.", sched.checkExists(triggerKey("t1")));

        job = sched.getJobDetail(jobKey("j1"));

        assertNotNull("Stored job not found!", job);
        
        trigger = sched.getTrigger(triggerKey("t1"));

        assertNotNull("Stored trigger not found!", trigger);

        job = newJob()
            .ofType(TestJob.class)
            .withIdentity("j2", "g1")
            .build();
    
        trigger = newTrigger()
            .withIdentity("t2", "g1")
            .forJob(job)
            .startNow()
            .withSchedule(simpleSchedule()
                    .repeatForever()
                    .withIntervalInSeconds(5))
             .build();

        sched.scheduleJob(job, trigger);
        
        job = newJob()
            .ofType(TestJob.class)
            .withIdentity("j3", "g1")
            .build();
    
        trigger = newTrigger()
            .withIdentity("t3", "g1")
            .forJob(job)
            .startNow()
            .withSchedule(simpleSchedule()
                    .repeatForever()
                    .withIntervalInSeconds(5))
             .build();
    
        sched.scheduleJob(job, trigger);
        
                
        List<String> jobGroups = sched.getJobGroupNames();
        List<String> triggerGroups = sched.getTriggerGroupNames();
        
        assertTrue("Job group list size expected to be = 2 ", jobGroups.size() == 2);
        assertTrue("Trigger group list size expected to be = 2 ", triggerGroups.size() == 2);
        
        List<JobKey> jobKeys = sched.getJobKeys(JobKey.DEFAULT_GROUP);
        List<TriggerKey> triggerKeys = sched.getTriggerKeys(TriggerKey.DEFAULT_GROUP);

        assertTrue("Number of jobs expected in default group was 1 ", jobKeys.size() == 1);
        assertTrue("Number of triggers expected in default group was 1 ", triggerKeys.size() == 1);

        jobKeys = sched.getJobKeys("g1");
        triggerKeys = sched.getTriggerKeys("g1");

        assertTrue("Number of jobs expected in 'g1' group was 2 ", jobKeys.size() == 2);
        assertTrue("Number of triggers expected in 'g1' group was 2 ", triggerKeys.size() == 2);

        
        TriggerState s = sched.getTriggerState(triggerKey("t2", "g1"));
        assertTrue("State of trigger t2 expected to be NORMAL ", s.equals(TriggerState.STATE_NORMAL));
        
        sched.pauseTrigger(triggerKey("t2", "g1"));
        s = sched.getTriggerState(triggerKey("t2", "g1"));
        assertTrue("State of trigger t2 expected to be PAUSED ", s.equals(TriggerState.STATE_PAUSED));

        sched.resumeTrigger(triggerKey("t2", "g1"));
        s = sched.getTriggerState(triggerKey("t2", "g1"));
        assertTrue("State of trigger t2 expected to be NORMAL ", s.equals(TriggerState.STATE_NORMAL));

        Set<String> pausedGroups = sched.getPausedTriggerGroups();
        assertTrue("Size of paused trigger groups list expected to be 0 ", pausedGroups.size() == 0);
        
        sched.pauseTriggerGroup("g1");
        
        // test that adding a trigger to a paused group causes the new trigger to be paused also... 
        job = newJob()
            .ofType(TestJob.class)
            .withIdentity("j4", "g1")
            .build();
    
        trigger = newTrigger()
            .withIdentity("t4", "g1")
            .forJob(job)
            .startNow()
            .withSchedule(simpleSchedule()
                    .repeatForever()
                    .withIntervalInSeconds(5))
             .build();
    
        sched.scheduleJob(job, trigger);

        pausedGroups = sched.getPausedTriggerGroups();
        assertTrue("Size of paused trigger groups list expected to be 1 ", pausedGroups.size() == 1);

        s = sched.getTriggerState(triggerKey("t2", "g1"));
        assertTrue("State of trigger t2 expected to be PAUSED ", s.equals(TriggerState.STATE_PAUSED));

        s = sched.getTriggerState(triggerKey("t4", "g1"));
        assertTrue("State of trigger t4 expected to be PAUSED ", s.equals(TriggerState.STATE_PAUSED));
        
        sched.resumeTriggerGroup("g1");
        s = sched.getTriggerState(triggerKey("t2", "g1"));
        assertTrue("State of trigger t2 expected to be NORMAL ", s.equals(TriggerState.STATE_NORMAL));
        s = sched.getTriggerState(triggerKey("t4", "g1"));
        assertTrue("State of trigger t4 expected to be NORMAL ", s.equals(TriggerState.STATE_NORMAL));
        pausedGroups = sched.getPausedTriggerGroups();
        assertTrue("Size of paused trigger groups list expected to be 0 ", pausedGroups.size() == 0);

        
        assertFalse("Scheduler should have returned 'false' from attempt to unschedule non-existing trigger. ", sched.unscheduleJob(triggerKey("foasldfksajdflk")));

        assertTrue("Scheduler should have returned 'true' from attempt to unschedule existing trigger. ", sched.unscheduleJob(triggerKey("t3", "g1")));
        
        jobKeys = sched.getJobKeys("g1");
        triggerKeys = sched.getTriggerKeys("g1");

        assertTrue("Number of jobs expected in 'g1' group was 1 ", jobKeys.size() == 2); // job should have been deleted also, because it is non-durable
        assertTrue("Number of triggers expected in 'g1' group was 1 ", triggerKeys.size() == 2);

        assertTrue("Scheduler should have returned 'true' from attempt to unschedule existing trigger. ", sched.unscheduleJob(triggerKey("t1")));
        
        jobKeys = sched.getJobKeys(JobKey.DEFAULT_GROUP);
        triggerKeys = sched.getTriggerKeys(TriggerKey.DEFAULT_GROUP);

        assertTrue("Number of jobs expected in default group was 1 ", jobKeys.size() == 1); // job should have been left in place, because it is non-durable
        assertTrue("Number of triggers expected in default group was 0 ", triggerKeys.size() == 0);

        
    }

}
