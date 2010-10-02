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
 * 
 */
package org.quartz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.UnableToInterruptJobException;
import org.quartz.spi.JobFactory;

/**
 * <p>
 * An implementation of the <code>Scheduler</code> interface that remotely
 * proxies all method calls to the equivalent call on a given <code>QuartzScheduler</code>
 * instance, via JMX.
 * </p>
 * 
 * <p>
 * A user must create a subclass to implement the actual connection to the remote 
 * MBeanServer using their application specific connector.
 * For example <code>{@link org.quartz.ee.jmx.jboss.JBoss4RMIRemoteMBeanScheduler}</code>.
 * </p>
 * @see org.quartz.Scheduler
 * @see org.quartz.core.QuartzScheduler
 * @see org.quartz.core.SchedulingContext
 */
public abstract class RemoteMBeanScheduler implements Scheduler {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private ObjectName schedulerObjectName;
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public RemoteMBeanScheduler() { 
    }
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Properties.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    
    /**
     * Get the name under which the Scheduler MBean is registered on the
     * remote MBean server.
     */
    protected ObjectName getSchedulerObjectName() {
        return schedulerObjectName;
    }

    /**
     * Set the name under which the Scheduler MBean is registered on the
     * remote MBean server.
     */
    public void setSchedulerObjectName(String schedulerObjectName)  throws SchedulerException {
        try {
            this.schedulerObjectName = new ObjectName(schedulerObjectName);
        } catch (MalformedObjectNameException e) {
            throw new SchedulerException("Failed to parse Scheduler MBean name: " + schedulerObjectName, e);
        }
    }

    /**
     * Set the name under which the Scheduler MBean is registered on the
     * remote MBean server.
     */
    public void setSchedulerObjectName(ObjectName schedulerObjectName)  throws SchedulerException {
        this.schedulerObjectName = schedulerObjectName;
    }

    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Abstract methods.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Initialize this RemoteMBeanScheduler instance, connecting to the
     * remote MBean server.
     */
    public abstract void initialize() throws SchedulerException;

    /**
     * Get the given attribute of the remote Scheduler MBean.
     */
    protected abstract Object getAttribute(
            String attribute) throws SchedulerException;
        
    /**
     * Get the given attributes of the remote Scheduler MBean.
     */
    protected abstract AttributeList getAttributes(String[] attributes)
        throws SchedulerException;
    
    /**
     * Invoke the given operation on the remote Scheduler MBean.
     */
    protected abstract Object invoke(
        String operationName,
        Object[] params,
        String[] signature) throws SchedulerException;
        

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Returns the name of the <code>Scheduler</code>.
     * </p>
     */
    public String getSchedulerName() throws SchedulerException {
        return (String)getAttribute("schedulerName");
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    public String getSchedulerInstanceId() throws SchedulerException {
        return (String)getAttribute("schedulerInstanceId");
    }

    public SchedulerMetaData getMetaData() throws SchedulerException {
        AttributeList attributeList = 
            getAttributes(
                new String[] {
                    "schedulerName",
                    "schedulerInstanceId",
                    "inStandbyMode",
                    "shutdown",
                    "jobStoreClass",
                    "threadPoolClass",
                    "threadPoolSize",
                    "version"
                });
        
        return new SchedulerMetaData(
                (String)attributeList.get(0),
                (String)attributeList.get(1),
                getClass(), true, isStarted(), 
                ((Boolean)attributeList.get(2)).booleanValue(), 
                ((Boolean)attributeList.get(3)).booleanValue(), 
                (Date)invoke("runningSince", new Object[] {}, new String[] {}), 
                ((Integer)invoke("numJobsExecuted", new Object[] {}, new String[] {})).intValue(),
                (Class)attributeList.get(4),
                ((Boolean)invoke("supportsPersistence", new Object[] {}, new String[] {})).booleanValue(),
                ((Boolean)invoke("isClustered", new Object[] {}, new String[] {})).booleanValue(),
                (Class)attributeList.get(5),
                ((Integer)attributeList.get(6)).intValue(),
                (String)attributeList.get(7));
    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    public SchedulerContext getContext() throws SchedulerException {
        return (SchedulerContext)getAttribute("schedulerContext");
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Schedululer State Management Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void start() throws SchedulerException {
        invoke("start", new Object[] {}, new String[] {});
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void startDelayed(int seconds) throws SchedulerException {
        invoke("startDelayed", new Object[] {new Integer(seconds)}, new String[] {int.class.getName()});
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void standby() throws SchedulerException {
        invoke("standby", new Object[] {}, new String[] {});
    }

    /**
     * Whether the scheduler has been started.  
     * 
     * <p>
     * Note: This only reflects whether <code>{@link #start()}</code> has ever
     * been called on this Scheduler, so it will return <code>true</code> even 
     * if the <code>Scheduler</code> is currently in standby mode or has been 
     * since shutdown.
     * </p>
     * 
     * @see #start()
     * @see #isShutdown()
     * @see #isInStandbyMode()
     */    
    public boolean isStarted() throws SchedulerException {
        return (invoke("runningSince", new Object[] {}, new String[] {}) != null);
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean isInStandbyMode() throws SchedulerException {
        return ((Boolean)getAttribute("inStandbyMode")).booleanValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void shutdown() throws SchedulerException {
        // Have to get the scheduler name before we actually call shutdown.
        String schedulerName = getSchedulerName();
        
        invoke("shutdown", new Object[] {}, new String[] {});
        SchedulerRepository.getInstance().remove(schedulerName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void shutdown(boolean waitForJobsToComplete)
        throws SchedulerException {
        // Have to get the scheduler name before we actually call shutdown.
        String schedulerName = getSchedulerName();
        
        invoke(
            "shutdown", 
            new Object[] { toBoolean(waitForJobsToComplete) }, 
            new String[] { boolean.class.getName() });
        
        SchedulerRepository.getInstance().remove(schedulerName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean isShutdown() throws SchedulerException {
        return ((Boolean)getAttribute("shutdown")).booleanValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException {
        return (List)invoke("getCurrentlyExecutingJobs", new Object[] {}, new String[] {});
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Scheduling-related Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger)
        throws SchedulerException {
        return (Date)invoke(
                "scheduleJob", 
                new Object[] { jobDetail, trigger }, 
                new String[] { JobDetail.class.getName(), Trigger.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        return (Date)invoke(
                "scheduleJob", 
                new Object[] { trigger }, 
                new String[] { Trigger.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void addJob(JobDetail jobDetail, boolean replace)
        throws SchedulerException {
        invoke(
            "addJob", 
            new Object[] { jobDetail, toBoolean(replace) }, 
            new String[] { JobDetail.class.getName(), boolean.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean deleteJob(JobKey jobKey)
        throws SchedulerException {
        return ((Boolean)invoke(
                "deleteJob", 
                new Object[] { jobKey.getName(), jobKey.getGroup()}, 
                new String[] { String.class.getName(), String.class.getName() })).booleanValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean unscheduleJob(TriggerKey triggerKey)
        throws SchedulerException {
        return ((Boolean)invoke(
                "unscheduleJob", 
                new Object[] { triggerKey.getName(), triggerKey.getGroup() }, 
                new String[] { String.class.getName(), String.class.getName() })).booleanValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Date rescheduleJob(TriggerKey triggerKey,
            Trigger newTrigger) throws SchedulerException {
        return (Date)invoke(
                "unscheduleJob", 
                new Object[] { triggerKey.getName(), triggerKey.getGroup(), newTrigger}, 
                new String[] { String.class.getName(), String.class.getName(), Trigger.class.getName() });
    }
    
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJob(JobKey jobKey)
        throws SchedulerException {
        triggerJob(jobKey, null);
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJob(JobKey jobKey, JobDataMap data)
        throws SchedulerException {
        invoke(
            "triggerJob", 
            new Object[] { jobKey.getName(), jobKey.getGroup(), data}, 
            new String[] { String.class.getName(), String.class.getName(), JobDataMap.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJobWithVolatileTrigger(String jobName, String groupName)
        throws SchedulerException {
        triggerJobWithVolatileTrigger(jobName, groupName, null);
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJobWithVolatileTrigger(String jobName, String groupName, JobDataMap data)
        throws SchedulerException {
        invoke(
            "triggerJobWithVolatileTrigger", 
            new Object[] { jobName, groupName, data}, 
            new String[] { String.class.getName(), String.class.getName(), JobDataMap.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseTrigger(TriggerKey triggerKey)
        throws SchedulerException {
        invoke(
            "pauseTrigger", 
            new Object[] { triggerKey.getName(), triggerKey.getGroup() }, 
            new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseTriggerGroup(String groupName) throws SchedulerException {
        invoke(
            "pauseTriggerGroup", 
            new Object[] { groupName}, 
            new String[] { String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseJob(JobKey jobKey)
        throws SchedulerException {
        invoke(
            "pauseJob", 
            new Object[] { jobKey.getName(), jobKey.getGroup() }, 
            new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseJobGroup(String groupName) throws SchedulerException {
        invoke(
            "pauseJobGroup", 
            new Object[] { groupName}, 
            new String[] { String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeTrigger(TriggerKey triggerKey)
        throws SchedulerException {
        invoke(
            "resumeTrigger", 
            new Object[] { triggerKey.getName(), triggerKey.getGroup() }, 
            new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeTriggerGroup(String groupName) throws SchedulerException {
        invoke(
            "resumeTriggerGroup", 
            new Object[] { groupName}, 
            new String[] { String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeJob(JobKey jobKey)
        throws SchedulerException {
        invoke(
            "resumeJob", 
            new Object[] { jobKey.getName(), jobKey.getGroup() }, 
            new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeJobGroup(String groupName) throws SchedulerException {
        invoke(
            "resumeJobGroup", 
            new Object[] { groupName}, 
            new String[] { String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseAll() throws SchedulerException {
        invoke(
            "pauseAll", 
            new Object[] { }, 
            new String[] { });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeAll() throws SchedulerException {
        invoke(
            "resumeAll", 
            new Object[] { }, 
            new String[] { });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getJobGroupNames() throws SchedulerException {
        return (List<String>) invoke(
                "getJobGroupNames", 
                new Object[] { }, 
                new String[] { });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<JobKey> getJobKeys(String groupName) throws SchedulerException {
        List<String> names = (List<String>)invoke(
                "getJobNames", 
                new Object[] { groupName }, 
                new String[] { String.class.getName() });
        
        List<JobKey> keys = new ArrayList<JobKey>(names.size());
        for(String name: names)
            keys.add(new JobKey(name, groupName));
        
        return keys;
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<Trigger> getTriggersOfJob(JobKey jobKey)
        throws SchedulerException {
        return (List<Trigger>)invoke(
                "getTriggersOfJob", 
                new Object[] { jobKey.getName(), jobKey.getGroup() }, 
                new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getTriggerGroupNames() throws SchedulerException {
        return (List<String>)invoke(
                "getTriggerGroupNames", 
                new Object[] { }, 
                new String[] { });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<TriggerKey> getTriggerKeys(String groupName) throws SchedulerException {
        List<String> names =  (List<String>)invoke(
                "getTriggerNames", 
                new Object[] { groupName }, 
                new String[] { String.class.getName() });
        List<TriggerKey> keys = new ArrayList<TriggerKey>(names.size());
        for(String name: names)
            keys.add(new TriggerKey(name, groupName));
        
        return keys;
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public JobDetail getJobDetail(JobKey jobKey)
        throws SchedulerException {
        return (JobDetail)invoke(
                "getJobDetail", 
                new Object[] { jobKey.getName(), jobKey.getGroup() }, 
                new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Trigger getTrigger(TriggerKey triggerKey)
        throws SchedulerException {
        return (Trigger)invoke(
                "getTrigger", 
                new Object[] { triggerKey.getName(), triggerKey.getGroup() }, 
                new String[] { String.class.getName(), String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public int getTriggerState(TriggerKey triggerKey)
        throws SchedulerException {
        return ((Integer)invoke(
                "getTriggerState", 
                new Object[] { triggerKey.getName(), triggerKey.getGroup() }, 
                new String[] { String.class.getName(), String.class.getName() })).intValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
        throws SchedulerException {
        invoke(
            "addCalendar", 
            new Object[] { calName, calendar, toBoolean(replace), toBoolean(updateTriggers) }, 
            new String[] { String.class.getName(), 
                    Calendar.class.getName(), boolean.class.getName(), boolean.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean deleteCalendar(String calName) throws SchedulerException {
        return ((Boolean)invoke(
                "getTriggerState", 
                new Object[] { calName }, 
                new String[] { String.class.getName() })).booleanValue();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Calendar getCalendar(String calName) throws SchedulerException {
        return (Calendar)invoke(
                "getCalendar", 
                new Object[] { calName }, 
                new String[] { String.class.getName() });
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getCalendarNames() throws SchedulerException {
        return (List<String>)invoke(
                "getCalendarNames", 
                new Object[] { }, 
                new String[] { });
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Listener-related Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addGlobalJobListener(JobListener jobListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addJobListener(JobListener jobListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean removeGlobalJobListener(String name)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean removeJobListener(String name) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List<JobListener> getGlobalJobListeners() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public Set<String> getJobListenerNames() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public JobListener getGlobalJobListener(String name) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public JobListener getJobListener(String name) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addGlobalTriggerListener(TriggerListener triggerListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addTriggerListener(TriggerListener triggerListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean removeGlobalTriggerListener(String name)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean removeTriggerListener(String name) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List<TriggerListener> getGlobalTriggerListeners() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public Set<String> getTriggerListenerNames() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public TriggerListener getGlobalTriggerListener(String name)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public TriggerListener getTriggerListener(String name)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addSchedulerListener(SchedulerListener schedulerListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean removeSchedulerListener(SchedulerListener schedulerListener)
        throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List<SchedulerListener> getSchedulerListeners() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /** 
     * @see org.quartz.Scheduler#getPausedTriggerGroups()
     */
    public Set<String> getPausedTriggerGroups() throws SchedulerException {
        return (Set<String>)invoke(
                "getPausedTriggerGroups", 
                new Object[] { }, 
                new String[] { });
    }

    /**
     * @see org.quartz.Scheduler#interrupt(JobKey)
     */
    public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException  {
        try {
            return ((Boolean)invoke(
                    "interrupt", 
                    new Object[] { jobKey.getName(), jobKey.getGroup() }, 
                    new String[] { String.class.getName(), String.class.getName() })).booleanValue();
        } catch (SchedulerException se) {
            throw new UnableToInterruptJobException(se);
        }
    }

    /**
     * @see org.quartz.Scheduler#setJobFactory(org.quartz.spi.JobFactory)
     */
    public void setJobFactory(JobFactory factory) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }
    
    protected Boolean toBoolean(boolean bool) {
        return (bool) ? Boolean.TRUE : Boolean.FALSE;
    }
}
