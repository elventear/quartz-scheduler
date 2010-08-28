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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.UnableToInterruptJobException;
import org.quartz.core.RemotableQuartzScheduler;
import org.quartz.spi.JobFactory;

/**
 * <p>
 * An implementation of the <code>Scheduler</code> interface that remotely
 * proxies all method calls to the equivalent call on a given <code>QuartzScheduler</code>
 * instance, via RMI.
 * </p>
 * 
 * @see org.quartz.Scheduler
 * @see org.quartz.core.QuartzScheduler
 * @see org.quartz.core.SchedulingContext
 * 
 * @author James House
 */
public class RemoteScheduler implements Scheduler {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private RemotableQuartzScheduler rsched;

    private String schedId;

    private String rmiHost;

    private int rmiPort;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Construct a <code>RemoteScheduler</code> instance to proxy the given
     * <code>RemoteableQuartzScheduler</code> instance, and with the given
     * <code>SchedulingContext</code>.
     * </p>
     */
    public RemoteScheduler(String schedId, String host, int port) {
        this.schedId = schedId;
        this.rmiHost = host;
        this.rmiPort = port;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected RemotableQuartzScheduler getRemoteScheduler()
        throws SchedulerException {
        if (rsched != null) {
            return rsched;
        }

        try {
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);

            rsched = (RemotableQuartzScheduler) registry.lookup(schedId);

        } catch (Exception e) {
            SchedulerException initException = new SchedulerException(
                    "Could not get handle to remote scheduler: "
                            + e.getMessage(), e);
            throw initException;
        }

        return rsched;
    }

    protected SchedulerException invalidateHandleCreateException(String msg,
            Exception cause) {
        rsched = null;
        SchedulerException ex = new SchedulerException(msg, cause);
        return ex;
    }

    /**
     * <p>
     * Returns the name of the <code>Scheduler</code>.
     * </p>
     */
    public String getSchedulerName() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerName();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    public String getSchedulerInstanceId() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerInstanceId();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    public SchedulerMetaData getMetaData() throws SchedulerException {
        try {
            RemotableQuartzScheduler sched = getRemoteScheduler();
            return new SchedulerMetaData(getSchedulerName(),
                    getSchedulerInstanceId(), getClass(), true, isStarted(), 
                    isInStandbyMode(), isShutdown(), sched.runningSince(), 
                    sched.numJobsExecuted(), sched.getJobStoreClass(), 
                    sched.supportsPersistence(), sched.isClustered(), sched.getThreadPoolClass(), 
                    sched.getThreadPoolSize(), sched.getVersion());

        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }

    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    public SchedulerContext getContext() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerContext();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            getRemoteScheduler().start();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void startDelayed(int seconds) throws SchedulerException {
        try {
            getRemoteScheduler().startDelayed(seconds);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void standby() throws SchedulerException {
        try {
            getRemoteScheduler().standby();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            return (getRemoteScheduler().runningSince() != null);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }   
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean isInStandbyMode() throws SchedulerException {
        try {
            return getRemoteScheduler().isInStandbyMode();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void shutdown() throws SchedulerException {
        try {
            String schedulerName = getSchedulerName();
            
            getRemoteScheduler().shutdown();
            
            SchedulerRepository.getInstance().remove(schedulerName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void shutdown(boolean waitForJobsToComplete)
        throws SchedulerException {
        try {
            String schedulerName = getSchedulerName();
            
            getRemoteScheduler().shutdown(waitForJobsToComplete);

            SchedulerRepository.getInstance().remove(schedulerName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean isShutdown() throws SchedulerException {
        try {
            return getRemoteScheduler().isShutdown();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List getCurrentlyExecutingJobs() throws SchedulerException {
        try {
            return getRemoteScheduler().getCurrentlyExecutingJobs();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            return getRemoteScheduler().scheduleJob(jobDetail,
                    trigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        try {
            return getRemoteScheduler().scheduleJob(trigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            getRemoteScheduler().addJob(jobDetail, replace);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean deleteJob(String jobName, String groupName)
        throws SchedulerException {
        try {
            return getRemoteScheduler()
                    .deleteJob(jobName, groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean unscheduleJob(String triggerName, String groupName)
        throws SchedulerException {
        try {
            return getRemoteScheduler().unscheduleJob(triggerName,
                    groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Date rescheduleJob(String triggerName,
            String groupName, Trigger newTrigger) throws SchedulerException {
        try {
            return getRemoteScheduler().rescheduleJob(triggerName,
                    groupName, newTrigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }
    
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJob(String jobName, String groupName)
        throws SchedulerException {
        triggerJob(jobName, groupName, null);
    }
    
    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void triggerJob(String jobName, String groupName, JobDataMap data)
        throws SchedulerException {
        try {
            getRemoteScheduler().triggerJob(jobName, groupName, data);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            getRemoteScheduler().triggerJobWithVolatileTrigger(jobName, groupName, data);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseTrigger(String triggerName, String groupName)
        throws SchedulerException {
        try {
            getRemoteScheduler()
                    .pauseTrigger(triggerName, groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseTriggerGroup(String groupName) throws SchedulerException {
        try {
            getRemoteScheduler().pauseTriggerGroup(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseJob(String jobName, String groupName)
        throws SchedulerException {
        try {
            getRemoteScheduler().pauseJob(jobName, groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseJobGroup(String groupName) throws SchedulerException {
        try {
            getRemoteScheduler().pauseJobGroup(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeTrigger(String triggerName, String groupName)
        throws SchedulerException {
        try {
            getRemoteScheduler().resumeTrigger(triggerName,
                    groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeTriggerGroup(String groupName) throws SchedulerException {
        try {
            getRemoteScheduler().resumeTriggerGroup(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeJob(String jobName, String groupName)
        throws SchedulerException {
        try {
            getRemoteScheduler().resumeJob(jobName, groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeJobGroup(String groupName) throws SchedulerException {
        try {
            getRemoteScheduler().resumeJobGroup(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void pauseAll() throws SchedulerException {
        try {
            getRemoteScheduler().pauseAll();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public void resumeAll() throws SchedulerException {
        try {
            getRemoteScheduler().resumeAll();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getJobGroupNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getJobGroupNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getJobNames(String groupName) throws SchedulerException {
        try {
            return getRemoteScheduler().getJobNames(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<Trigger> getTriggersOfJob(String jobName, String groupName)
        throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggersOfJob(jobName,
                    groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getTriggerGroupNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerGroupNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getTriggerNames(String groupName) throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerNames(groupName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public JobDetail getJobDetail(String jobName, String jobGroup)
        throws SchedulerException {
        try {
            return getRemoteScheduler().getJobDetail(jobName,
                    jobGroup);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Trigger getTrigger(String triggerName, String triggerGroup)
        throws SchedulerException {
        try {
            return getRemoteScheduler().getTrigger(triggerName,
                    triggerGroup);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public int getTriggerState(String triggerName, String triggerGroup)
        throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerState(triggerName,
                    triggerGroup);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            getRemoteScheduler().addCalendar(calName, calendar,
                    replace, updateTriggers);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public boolean deleteCalendar(String calName) throws SchedulerException {
        try {
            return getRemoteScheduler().deleteCalendar(calName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public Calendar getCalendar(String calName) throws SchedulerException {
        try {
            return getRemoteScheduler().getCalendar(calName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>,
     * passing the <code>SchedulingContext</code> associated with this
     * instance.
     * </p>
     */
    public List<String> getCalendarNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getCalendarNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
    public List getGlobalJobListeners() throws SchedulerException {
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
    public List getGlobalTriggerListeners() throws SchedulerException {
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
    public List getSchedulerListeners() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /** 
     * @see org.quartz.Scheduler#getPausedTriggerGroups()
     */
    public Set getPausedTriggerGroups() throws SchedulerException {
        try {
            return getRemoteScheduler().getPausedTriggerGroups();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * @see org.quartz.Scheduler#interrupt(java.lang.String, java.lang.String)
     */
    public boolean interrupt(String jobName, String groupName) throws UnableToInterruptJobException  {
        try {
            return getRemoteScheduler().interrupt(jobName, groupName);
        } catch (RemoteException re) {
            throw new UnableToInterruptJobException(invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re));
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
}
