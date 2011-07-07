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

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerKey.*;

/**
 * Test job interruption
 */
public class InterruptableJobTest extends TestCase {

    static final CyclicBarrier sync = new CyclicBarrier(2);

    public static class TestInterruptableJob implements InterruptableJob {

        public static volatile boolean interrupted = false;
        
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            System.out.println("TestInterruptableJob is executing.");
            TestInterruptableJob.interrupted = false;
            for(int i=0; i < 100; i++) {
                if(TestInterruptableJob.interrupted) break;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ingore) { }
            }
            try {
                System.out.println("TestInterruptableJob exiting with interrupted = " + interrupted);
                sync.await();
            } catch (InterruptedException e) {
            } catch (BrokenBarrierException e) {
            }
        }

        public void interrupt() throws UnableToInterruptJobException {
            TestInterruptableJob.interrupted = true;
            System.out.println("TestInterruptableJob.interrupt() called.");
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
        sched.start();

        // test basic storage functions of scheduler...
        
        JobDetail job = newJob()
            .ofType(TestInterruptableJob.class)
            .withIdentity("j1")
            .storeDurably()
            .build();

        Trigger trigger = newTrigger()
            .withIdentity("t1")
            .forJob(job)
            .startNow()
            .build();

        sched.scheduleJob(job, trigger);
        
        Thread.sleep(500); // make sure the job starts running...
        
        List<JobExecutionContext> executingJobs = sched.getCurrentlyExecutingJobs();
        
        assertTrue("Number of executing jobs should be 1 ", executingJobs.size() == 1);
        
        JobExecutionContext jec = executingJobs.get(0);
        
        boolean interruptResult = sched.interrupt(jec.getFireInstanceId());
        
        sync.await();

        assertTrue("Expected successful result from interruption of job ", interruptResult);

        assertTrue("Expected interrupted flag to be set job class ", TestInterruptableJob.interrupted);

        sched.shutdown();
    }

}
