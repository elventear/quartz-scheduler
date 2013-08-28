/*
 * Copyright 2001-2013 Terracotta, Inc.
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
package org.quartz.integrations.tests;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A integration test for Quartz Database Scheduler with Cron Trigger.
 * @author Zemian Deng
 */
public class QuartzDatabaseCronTriggerTest extends QuartzDatabaseTestSupport {
    @Test
    public void testCronRepeatCount() throws Exception {
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("test")
                .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
                .build();
        JobDetail jobDetail = JobBuilder.newJob(CountingJob.class).withIdentity("test").build();
        scheduler.scheduleJob(jobDetail, trigger);

        // Give it enough time to run
        Thread.sleep(3500L);

        AtomicInteger counter = (AtomicInteger)scheduler.getContext().get(CountingJob.COUNTER_KEY);
        Assert.assertThat(counter.intValue(), Matchers.greaterThanOrEqualTo(3));
    }
}
