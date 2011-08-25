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
package org.quartz.core;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.quartz.JobListener;
import org.quartz.TriggerKey;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.NameMatcher;

import static org.quartz.impl.matchers.GroupMatcher.*;
import static org.quartz.impl.matchers.NameMatcher.*;
import org.quartz.listeners.JobListenerSupport;
import org.quartz.listeners.SchedulerListenerSupport;
import org.quartz.listeners.TriggerListenerSupport;

import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerKey.*;

/**
 * Test ListenerManagerImpl functionality 
 */
public class ListenerManagerTest extends TestCase {


    public static class TestJobListner extends JobListenerSupport {

        private String name;
        
        public TestJobListner(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    public static class TestTriggerListner extends TriggerListenerSupport {

        private String name;
        
        public TestTriggerListner(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    public static class TestSchedulerListner extends SchedulerListenerSupport {

    }

    @Override
    protected void setUp() throws Exception {
    }

    public void testManagementOfJobListeners() throws Exception {
        
        TestJobListner tl1 = new TestJobListner("tl1");
        TestJobListner tl2 = new TestJobListner("tl2");
        
        ListenerManagerImpl manager = new ListenerManagerImpl();

        // test adding listener without matcher
        manager.addJobListener(tl1);
        assertEquals("Unexpected size of listener list", 1, manager.getJobListeners().size());

        // test adding listener with matcher
        manager.addJobListener(tl2, jobGroupEquals("foo"));
        assertEquals("Unexpected size of listener list", 2, manager.getJobListeners().size());

        // test removing a listener
        manager.removeJobListener("tl1");
        assertEquals("Unexpected size of listener list", 1, manager.getJobListeners().size());
        
        // test adding a matcher
        manager.addJobListenerMatcher("tl2", jobNameContains("foo"));
        assertEquals("Unexpected size of listener's matcher list", 2, manager.getJobListenerMatchers("tl2").size());
    }

    public void testManagementOfTriggerListeners() throws Exception {
        
        TestTriggerListner tl1 = new TestTriggerListner("tl1");
        TestTriggerListner tl2 = new TestTriggerListner("tl2");
        
        ListenerManagerImpl manager = new ListenerManagerImpl();

        // test adding listener without matcher
        manager.addTriggerListener(tl1);
        assertEquals("Unexpected size of listener list", 1, manager.getTriggerListeners().size());

        // test adding listener with matcher
        manager.addTriggerListener(tl2, triggerGroupEquals("foo"));
        assertEquals("Unexpected size of listener list", 2, manager.getTriggerListeners().size());

        // test removing a listener
        manager.removeTriggerListener("tl1");
        assertEquals("Unexpected size of listener list", 1, manager.getTriggerListeners().size());
        
        // test adding a matcher
        manager.addTriggerListenerMatcher("tl2", NameMatcher.<TriggerKey>nameContains("foo"));
        assertEquals("Unexpected size of listener's matcher list", 2, manager.getTriggerListenerMatchers("tl2").size());
    }


    public void testManagementOfSchedulerListeners() throws Exception {
        
        TestSchedulerListner tl1 = new TestSchedulerListner();
        TestSchedulerListner tl2 = new TestSchedulerListner();
        
        ListenerManagerImpl manager = new ListenerManagerImpl();

        // test adding listener without matcher
        manager.addSchedulerListener(tl1);
        assertEquals("Unexpected size of listener list", 1, manager.getSchedulerListeners().size());

        // test adding listener with matcher
        manager.addSchedulerListener(tl2);
        assertEquals("Unexpected size of listener list", 2, manager.getSchedulerListeners().size());

        // test removing a listener
        manager.removeSchedulerListener(tl1);
        assertEquals("Unexpected size of listener list", 1, manager.getSchedulerListeners().size());
    }

}
