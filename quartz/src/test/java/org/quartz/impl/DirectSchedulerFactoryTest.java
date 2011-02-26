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
package org.quartz.impl;

import java.util.Collections;

import junit.framework.TestCase;

import org.quartz.Scheduler;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.spi.ThreadPool;

public class DirectSchedulerFactoryTest extends TestCase {
    public void testPlugins() throws Exception {
        final StringBuffer result = new StringBuffer();
        
        SchedulerPlugin testPlugin = new SchedulerPlugin() {
            public void initialize(String name, org.quartz.Scheduler scheduler) throws org.quartz.SchedulerException {
                result.append(name).append("|").append(scheduler.getSchedulerName());
            };
            public void start() {
                result.append("|start");
            };
            public void shutdown() {
                result.append("|shutdown");
            };
        };
        
        ThreadPool threadPool = new SimpleThreadPool(1, 5);
        threadPool.initialize();
        DirectSchedulerFactory.getInstance().createScheduler(
                "MyScheduler", "Instance1", threadPool,
                new RAMJobStore(), Collections.singletonMap("TestPlugin", testPlugin), 
                null, -1, 0, 0, false, null);
        
        Scheduler scheduler = DirectSchedulerFactory.getInstance().getScheduler("MyScheduler");
        scheduler.start();
        scheduler.shutdown();
        
        assertEquals("TestPlugin|MyScheduler|start|shutdown", result.toString());
    }
}
