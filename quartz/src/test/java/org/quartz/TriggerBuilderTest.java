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
import java.util.Date;
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
 * Test TriggerBuilder functionality
 */
public class TriggerBuilderTest extends TestCase {


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

    public void testTriggerBuilder() throws Exception {
        
        JobDetail job = newJob()
            .ofType(TestJob.class)
            .withIdentity("j1")
            .storeDurably()
            .build();
        
        Trigger trigger = newTrigger()
            .build();
        
        assertTrue("Expected non-null trigger name ", trigger.getKey().getName() != null);
        assertTrue("Unexpected trigger group: " + trigger.getKey().getGroup(), trigger.getKey().getGroup().equals(JobKey.DEFAULT_GROUP));
        assertTrue("Unexpected job key: " + trigger.getJobKey(), trigger.getJobKey() == null);
        assertTrue("Unexpected job description: " + trigger.getDescription(), trigger.getDescription() == null);
        assertTrue("Unexpected trigger priortiy: " + trigger.getPriority(), trigger.getPriority() == Trigger.DEFAULT_PRIORITY);
        assertTrue("Unexpected start-time: " + trigger.getStartTime(), trigger.getStartTime() != null);
        assertTrue("Unexpected end-time: " + trigger.getEndTime(), trigger.getEndTime() == null);
        
        Date stime = evenSecondDateAfterNow();
        
        trigger = newTrigger()
            .withIdentity("t1")
            .withDescription("my description")
            .withPriority(2)
            .endAt(futureDate(10, IntervalUnit.WEEK))
            .startAt(stime)
            .build();
        
        assertTrue("Unexpected trigger name " + trigger.getKey().getName(), trigger.getKey().getName().equals("t1"));
        assertTrue("Unexpected trigger group: " + trigger.getKey().getGroup(), trigger.getKey().getGroup().equals(JobKey.DEFAULT_GROUP));
        assertTrue("Unexpected job key: " + trigger.getJobKey(), trigger.getJobKey() == null);
        assertTrue("Unexpected job description: " + trigger.getDescription(), trigger.getDescription().equals("my description"));
        assertTrue("Unexpected trigger priortiy: " + trigger, trigger.getPriority() == 2);
        assertTrue("Unexpected start-time: " + trigger.getStartTime(), trigger.getStartTime().equals(stime));
        assertTrue("Unexpected end-time: " + trigger.getEndTime(), trigger.getEndTime() != null);
        
    }

}
