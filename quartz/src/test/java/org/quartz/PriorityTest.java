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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Test Trigger priority support.
 */
public class PriorityTest extends TestCase {

    private static CountDownLatch latch;
    private static StringBuffer result;

    public static class TestJob implements StatefulJob {
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            result.append(context.getTrigger().getName());
            latch.countDown();
        }
    }

    @Override
    protected void setUp() throws Exception {
        latch = new CountDownLatch(2);
        result = new StringBuffer();
    }

    public void testSameDefaultPriority() throws Exception {
        Scheduler sched = StdSchedulerFactory.getDefaultScheduler();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 1);

        Trigger trig1 = new SimpleTrigger("T1", null, cal.getTime());
        Trigger trig2 = new SimpleTrigger("T2", null, cal.getTime());

        JobDetail jobDetail = new JobDetail("JD", null, TestJob.class);

        sched.scheduleJob(jobDetail, trig1);

        trig2.setJobName(jobDetail.getName());
        sched.scheduleJob(trig2);

        sched.start();

        latch.await();

        assertEquals("T1T2", result.toString());

        sched.shutdown();
    }

    public void testDifferentPriority() throws Exception {
        Scheduler sched = StdSchedulerFactory.getDefaultScheduler();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 1);

        Trigger trig1 = new SimpleTrigger("T1", null, cal.getTime());
        trig1.setPriority(5);

        Trigger trig2 = new SimpleTrigger("T2", null, cal.getTime());
        trig2.setPriority(10);

        JobDetail jobDetail = new JobDetail("JD", null, TestJob.class);

        sched.scheduleJob(jobDetail, trig1);

        trig2.setJobName(jobDetail.getName());
        sched.scheduleJob(trig2);

        sched.start();

        latch.await();

        assertEquals("T2T1", result.toString());

        sched.shutdown();
    }
}
