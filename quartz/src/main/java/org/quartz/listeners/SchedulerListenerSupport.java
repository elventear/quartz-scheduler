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
package org.quartz.listeners;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helpful abstract base class for implementors of 
 * <code>{@link org.quartz.SchedulerListener}</code>.
 * 
 * <p>
 * The methods in this class are empty so you only need to override the  
 * subset for the <code>{@link org.quartz.SchedulerListener}</code> events
 * you care about.
 * </p>
 * 
 * @see org.quartz.SchedulerListener
 */
public abstract class SchedulerListenerSupport implements SchedulerListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get the <code>{@link org.slf4j.Logger}</code> for this
     * class's category.  This should be used by subclasses for logging.
     */
    protected Logger getLog() {
        return log;
    }

    public void jobAdded(JobDetail jobDetail) {
    }
    
    public void jobDeleted(String jobName, String groupName) {
    }
    
    public void jobScheduled(Trigger trigger) {
    }

    public void jobUnscheduled(String triggerName, String triggerGroup) {
    }

    public void triggerFinalized(Trigger trigger) {
    }

    public void triggersPaused(String triggerName, String triggerGroup) {
    }

    public void triggersResumed(String triggerName, String triggerGroup) {
    }

    public void jobsPaused(String jobName, String jobGroup) {
    }

    public void jobsResumed(String jobName, String jobGroup) {
    }

    public void schedulerError(String msg, SchedulerException cause) {
    }

    public void schedulerStarted() {
    }
    
    public void schedulerInStandbyMode() {
    }
    
    public void schedulerShutdown() {
    }
    
    public void schedulerShuttingdown() {
    }
    
}
