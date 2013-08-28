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

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Job for testing. It will record the number of execution count into the scheduler
 * context with key "CountingJob.Counter".
 *
 * @author Zemian Deng
 */
public class CountingJob implements Job {
    protected static final Logger LOG = LoggerFactory.getLogger(CountingJob.class);
    public static String COUNTER_KEY = "CountingJob.Counter";
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Scheduler scheduler = context.getScheduler();
            AtomicInteger counter = (AtomicInteger)scheduler.getContext().get(COUNTER_KEY);
            if (counter == null) {
                counter = new AtomicInteger(0);
                scheduler.getContext().put(COUNTER_KEY, counter);
            }
            int value = counter.incrementAndGet();
            LOG.info("Incremented counter to " + value);
        } catch (SchedulerException e) {
            throw new JobExecutionException(e);
        }
    }
}
