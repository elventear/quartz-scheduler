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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.QueueJobDetail;
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
public class QuartzQueueThreadTest {
	@Test
    public void testWithDefaults() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory();
        Scheduler scheduler = fac.getScheduler();
        scheduler.start();
        Thread.sleep(5000);
        scheduler.shutdown();
    }
	
    @Test
    public void testQueueJobManager() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QuartzQueueThreadTest-mysql-quartz.properties");
        Scheduler scheduler = fac.getScheduler();
               
        QueueJobManager queueMgr = scheduler.getQueueJobManager();
        assertThat(queueMgr.getQueueJobDetails().size(), is(0));
        
        QueueJobDetailImpl job = new QueueJobDetailImpl();
        job.setDescription("Test job");
        job.setPriority(5);
        job.setKey(JobKey.jobKey("test"));
        job.setJobClass(MyQueueJob.class);
        queueMgr.addQueueJobDetail(job);
        assertThat(queueMgr.getQueueJobDetails().size(), is(1));
        
        queueMgr.removeQueueJobDetail(JobKey.jobKey("test"));
        assertThat(queueMgr.getQueueJobDetails().size(), is(0));
        
        // Add a batch
        for (int i=0; i < 100; i++) {
        	job = new QueueJobDetailImpl();
            job.setDescription("Test job" + i);
            job.setPriority(5);
            job.setKey(JobKey.jobKey("test" + i));
            job.setJobClass(MyQueueJob.class);
            queueMgr.addQueueJobDetail(job);
        }
        assertThat(queueMgr.getQueueJobDetails().size(), is(100));
        
        queueMgr.removeQueueJobDetail(JobKey.jobKey("test" + 50));
        assertThat(queueMgr.getQueueJobDetails().size(), is(99));
        
        job = (QueueJobDetailImpl)queueMgr.getQueueJobDetail(JobKey.jobKey("test" + 10));
        job.setPriority(9);
        queueMgr.updateQueueJobDetail(job);
        assertThat(queueMgr.getQueueJobDetails().size(), is(99));
        
        QueueJobDetail job2 = queueMgr.getQueueJobDetail(JobKey.jobKey("test" + 10));
        assertThat(job2.getPriority(), is(9));
        
        // Delete the batch
        for (int i=0; i < 100; i++) {
        	if (i == 50)
        		continue;
        	queueMgr.removeQueueJobDetail(JobKey.jobKey("test" + i));
        }
        assertThat(queueMgr.getQueueJobDetails().size(), is(0));
        
        scheduler.shutdown();
    }
    
    @Test
    public void testRunQueueJob() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QuartzQueueThreadTest-mysql-quartz.properties");
        Scheduler scheduler = fac.getScheduler();
        scheduler.start();
        
        Thread.sleep(30000L);
        scheduler.shutdown();
    }
    
    public static class MyQueueJob implements Job {

		public void execute(JobExecutionContext context) throws JobExecutionException {
			System.out.println("Executing job: " + context);
		}
    	
    }
}
