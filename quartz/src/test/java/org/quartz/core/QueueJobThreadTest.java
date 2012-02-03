/*
 * Copyright 2012 Terracotta, Inc.
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
package org.quartz.core;

import org.junit.Test;
import org.quartz.JobKey;
import org.quartz.QueueJob;
import org.quartz.QueueJobManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.QueueJobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Integration/Unit test for QueueJobThread.
 * 
 * @author Zemian Deng
 */
public class QueueJobThreadTest {
	
    @Test
    public void testAddQueueJob() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QueueJobThreadTest-mysql-quartz.properties");
        Scheduler scheduler = fac.getScheduler();
        
        QueueJobDetailImpl job = new QueueJobDetailImpl();
        job.setDescription("Test job");
        job.setPriority(5);
        job.setKey(JobKey.jobKey("test"));
        job.setQueueJobClass(MyQueueJob.class);
        
        QueueJobManager queueJobManager = scheduler.getQueueJobManager();
        queueJobManager.addQueueJobDetail(job);     
        
        scheduler.shutdown();
    }
    
    @Test
    public void testRunQueueJob() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QueueJobThreadTest-mysql-quartz.properties");
        Scheduler scheduler = fac.getScheduler();
        scheduler.start();
        
        Thread.sleep(30000L);
        scheduler.shutdown();
    }
    
    public static class MyQueueJob implements QueueJob {
    	
    }
}
