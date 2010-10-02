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
package org.quartz.simpl;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.jobs.NoOpJob;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.triggers.SimpleTriggerImpl;

/**
 * Unit test for RAMJobStore.  These tests were submitted by Johannes Zillmann
 * as part of issue QUARTZ-306.
 */
public class RAMJobStoreTest extends TestCase {
    private JobStore fJobStore;
    private JobDetail fJobDetail;
    private SampleSignaler fSignaler;

    protected void setUp() throws Exception {
        this.fJobStore = new RAMJobStore();
        this.fSignaler = new SampleSignaler();
        this.fJobStore.initialize(null, this.fSignaler);

        this.fJobDetail = new JobDetail("job1", "jobGroup1", NoOpJob.class);
        this.fJobDetail.setDurability(true);
        this.fJobStore.storeJob(this.fJobDetail, false);
    }

    public void testAcquireNextTrigger() throws Exception {
        OperableTrigger trigger1 = 
            new SimpleTriggerImpl("trigger1", "triggerGroup1", this.fJobDetail.getName(), 
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 200000), 
                    new Date(System.currentTimeMillis() + 200000), 2, 2000);
        OperableTrigger trigger2 = 
            new SimpleTriggerImpl("trigger2", "triggerGroup1", this.fJobDetail.getName(), 
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() - 100000),
                    new Date(System.currentTimeMillis() + 20000), 2, 2000);
        OperableTrigger trigger3 = 
            new SimpleTriggerImpl("trigger1", "triggerGroup2", this.fJobDetail.getName(), 
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 100000), 
                    new Date(System.currentTimeMillis() + 200000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(trigger1, false);
        this.fJobStore.storeTrigger(trigger2, false);
        this.fJobStore.storeTrigger(trigger3, false);

        assertTrue(this.fJobStore.acquireNextTriggers(10, 1, 1L).isEmpty());
        assertEquals(
            trigger2, 
            this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0));
        assertEquals(
            trigger3, 
            this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0));
        assertEquals(
            trigger1, 
            this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0));
        assertTrue(
            this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).isEmpty());

        // because of trigger2
        assertEquals(1, this.fSignaler.fMisfireCount);

        // release trigger3
        this.fJobStore.releaseAcquiredTrigger(trigger3);
        assertEquals(
            trigger3, 
            this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0));
    }

    public void testAcquireNextTriggerBatch() throws Exception {
        OperableTrigger trigger1 =
            new SimpleTriggerImpl("trigger1", "triggerGroup1", this.fJobDetail.getName(),
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 200000),
                    new Date(System.currentTimeMillis() + 200000), 2, 2000);
        OperableTrigger trigger2 =
            new SimpleTriggerImpl("trigger2", "triggerGroup1", this.fJobDetail.getName(),
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 200100),
                    new Date(System.currentTimeMillis() + 200100), 2, 2000);
        OperableTrigger trigger3 =
            new SimpleTriggerImpl("trigger3", "triggerGroup1", this.fJobDetail.getName(),
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 200200),
                    new Date(System.currentTimeMillis() + 200200), 2, 2000);
        OperableTrigger trigger4 =
            new SimpleTriggerImpl("trigger4", "triggerGroup1", this.fJobDetail.getName(),
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 200300),
                    new Date(System.currentTimeMillis() + 200300), 2, 2000);

        OperableTrigger trigger10 =
            new SimpleTriggerImpl("trigger10", "triggerGroup2", this.fJobDetail.getName(),
                    this.fJobDetail.getGroup(), new Date(System.currentTimeMillis() + 500000),
                    new Date(System.currentTimeMillis() + 700000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        trigger4.computeFirstFireTime(null);
        trigger10.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(trigger1, false);
        this.fJobStore.storeTrigger(trigger2, false);
        this.fJobStore.storeTrigger(trigger3, false);
        this.fJobStore.storeTrigger(trigger4, false);
        this.fJobStore.storeTrigger(trigger10, false);

        assertEquals(3, this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 3, 1000L).size());
        this.fJobStore.releaseAcquiredTrigger(trigger1);
        this.fJobStore.releaseAcquiredTrigger(trigger2);
        this.fJobStore.releaseAcquiredTrigger(trigger3);

        assertEquals(4, this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 4, 1000L).size());
        this.fJobStore.releaseAcquiredTrigger(trigger1);
        this.fJobStore.releaseAcquiredTrigger(trigger2);
        this.fJobStore.releaseAcquiredTrigger(trigger3);
        this.fJobStore.releaseAcquiredTrigger(trigger4);

        assertEquals(4, this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 5, 1000L).size());
        this.fJobStore.releaseAcquiredTrigger(trigger1);
        this.fJobStore.releaseAcquiredTrigger(trigger2);
        this.fJobStore.releaseAcquiredTrigger(trigger3);
        this.fJobStore.releaseAcquiredTrigger(trigger4);

        assertEquals(1, this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 0, 5, 0L).size());
        this.fJobStore.releaseAcquiredTrigger(trigger1);

        assertEquals(2, this.fJobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 150, 5, 0L).size());
        this.fJobStore.releaseAcquiredTrigger(trigger1);
        this.fJobStore.releaseAcquiredTrigger(trigger2);
    }

    public void testTriggerStates() throws Exception {
        OperableTrigger trigger = 
            new SimpleTriggerImpl("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail.getGroup(), 
                    new Date(System.currentTimeMillis() + 100000), new Date(System.currentTimeMillis() + 200000), 2, 2000);
        trigger.computeFirstFireTime(null);
        assertEquals(Trigger.STATE_NONE, this.fJobStore.getTriggerState(trigger.getKey()));
        this.fJobStore.storeTrigger(trigger, false);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(trigger.getKey()));
    
        this.fJobStore.pauseTrigger(trigger.getKey());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(trigger.getKey()));
    
        this.fJobStore.resumeTrigger(trigger.getKey());
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(trigger.getKey()));
    
        trigger = this.fJobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0);
        assertNotNull(trigger);
        this.fJobStore.releaseAcquiredTrigger(trigger);
        trigger=this.fJobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0);
        assertNotNull(trigger);
        assertTrue(this.fJobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).isEmpty());
    }

    // See: http://jira.opensymphony.com/browse/QUARTZ-606
    public void testStoreTriggerReplacesTrigger() throws Exception {

        String jobName = "StoreTriggerReplacesTrigger";
        String jobGroup = "StoreTriggerReplacesTriggerGroup";
        JobDetail detail = new JobDetail(jobName, jobGroup, NoOpJob.class);
        fJobStore.storeJob(detail, false);
 
        String trName = "StoreTriggerReplacesTrigger";
        String trGroup = "StoreTriggerReplacesTriggerGroup";
        OperableTrigger tr = new SimpleTriggerImpl(trName ,trGroup, new Date());
        tr.setJobKey(new JobKey(jobName, jobGroup));
        tr.setCalendarName(null);
 
        fJobStore.storeTrigger(tr, false);
        assertEquals(tr,fJobStore.retrieveTrigger(tr.getKey()));
 
        try {
            fJobStore.storeTrigger(tr, false);
            fail("an attempt to store duplicate trigger succeeded");
        } catch(ObjectAlreadyExistsException oaee) {
            // expected
        }

        tr.setCalendarName("QQ");
        fJobStore.storeTrigger(tr, true); //fails here
        assertEquals(tr, fJobStore.retrieveTrigger(tr.getKey()));
        assertEquals( "StoreJob doesn't replace triggers", "QQ", fJobStore.retrieveTrigger(tr.getKey()).getCalendarName());
    }

    public void testPauseJobGroupPausesNewJob() throws Exception
    {
    	final String jobName1 = "PauseJobGroupPausesNewJob";
    	final String jobName2 = "PauseJobGroupPausesNewJob2";
    	final String jobGroup = "PauseJobGroupPausesNewJobGroup";
    
    	JobDetail detail = new JobDetail(jobName1, jobGroup, NoOpJob.class);
    	detail.setDurability(true);
    	fJobStore.storeJob(detail, false);
    	fJobStore.pauseJobGroup(jobGroup);
    
    	detail = new JobDetail(jobName2, jobGroup, NoOpJob.class);
    	detail.setDurability(true);
    	fJobStore.storeJob(detail, false);
    
    	String trName = "PauseJobGroupPausesNewJobTrigger";
    	String trGroup = "PauseJobGroupPausesNewJobTriggerGroup";
    	OperableTrigger tr = new SimpleTriggerImpl(trName, trGroup, new Date());
        tr.setJobKey(new JobKey(jobName2, jobGroup));
    	fJobStore.storeTrigger(tr, false);
    	assertEquals(Trigger.STATE_PAUSED, fJobStore.getTriggerState(tr.getKey()));
    }
    
    public static class SampleSignaler implements SchedulerSignaler {
        int fMisfireCount = 0;

        public void notifyTriggerListenersMisfired(Trigger trigger) {
            fMisfireCount++;
        }

        public void signalSchedulingChange(long candidateNewNextFireTime) {
        }

        public void notifySchedulerListenersFinalized(Trigger trigger) {
        }
    }
}
