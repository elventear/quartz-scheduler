
/* 
 * Copyright 2001-2009 James House 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.UnableToInterruptJobException;

/**
 * @author James House
 */
public interface RemotableQuartzScheduler extends Remote {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    String getSchedulerName() throws RemoteException;

    String getSchedulerInstanceId() throws RemoteException;

    SchedulerContext getSchedulerContext() throws SchedulerException,
            RemoteException;

    void start() throws SchedulerException, RemoteException;

    void startDelayed(int seconds) throws SchedulerException, RemoteException;
    
    void standby() throws RemoteException;

    boolean isInStandbyMode() throws RemoteException;

    void shutdown() throws RemoteException;

    void shutdown(boolean waitForJobsToComplete) throws RemoteException;

    boolean isShutdown() throws RemoteException;

    Date runningSince() throws RemoteException;

    String getVersion() throws RemoteException;

    int numJobsExecuted() throws RemoteException;

    Class getJobStoreClass() throws RemoteException;

    boolean supportsPersistence() throws RemoteException;

    Class getThreadPoolClass() throws RemoteException;

    int getThreadPoolSize() throws RemoteException;

    List getCurrentlyExecutingJobs() throws SchedulerException,
            RemoteException;

    Date scheduleJob(SchedulingContext ctxt, JobDetail jobDetail,
            Trigger trigger) throws SchedulerException, RemoteException;

    Date scheduleJob(SchedulingContext ctxt, Trigger trigger)
        throws SchedulerException, RemoteException;

    void addJob(SchedulingContext ctxt, JobDetail jobDetail,
            boolean replace) throws SchedulerException, RemoteException;

    boolean deleteJob(SchedulingContext ctxt, String jobName,
            String groupName) throws SchedulerException, RemoteException;

    boolean unscheduleJob(SchedulingContext ctxt, String triggerName,
            String groupName) throws SchedulerException, RemoteException;

    Date rescheduleJob(SchedulingContext ctxt, String triggerName,
            String groupName, Trigger newTrigger) throws SchedulerException, RemoteException;
        
    
    void triggerJob(SchedulingContext ctxt, String jobName,
            String groupName, JobDataMap data) throws SchedulerException, RemoteException;

    void triggerJobWithVolatileTrigger(SchedulingContext ctxt,
            String jobName, String groupName, JobDataMap data) throws SchedulerException,
            RemoteException;

    void pauseTrigger(SchedulingContext ctxt, String triggerName,
            String groupName) throws SchedulerException, RemoteException;

    void pauseTriggerGroup(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    void pauseJob(SchedulingContext ctxt, String jobName,
            String groupName) throws SchedulerException, RemoteException;

    void pauseJobGroup(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    void resumeTrigger(SchedulingContext ctxt, String triggerName,
            String groupName) throws SchedulerException, RemoteException;

    void resumeTriggerGroup(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    Set getPausedTriggerGroups(SchedulingContext ctxt)
        throws SchedulerException, RemoteException;
    
    void resumeJob(SchedulingContext ctxt, String jobName,
            String groupName) throws SchedulerException, RemoteException;

    void resumeJobGroup(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    void pauseAll(SchedulingContext ctxt) throws SchedulerException,
            RemoteException;

    void resumeAll(SchedulingContext ctxt) throws SchedulerException,
            RemoteException;

    String[] getJobGroupNames(SchedulingContext ctxt)
        throws SchedulerException, RemoteException;

    String[] getJobNames(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    Trigger[] getTriggersOfJob(SchedulingContext ctxt, String jobName,
            String groupName) throws SchedulerException, RemoteException;

    String[] getTriggerGroupNames(SchedulingContext ctxt)
        throws SchedulerException, RemoteException;

    String[] getTriggerNames(SchedulingContext ctxt, String groupName)
        throws SchedulerException, RemoteException;

    JobDetail getJobDetail(SchedulingContext ctxt, String jobName,
            String jobGroup) throws SchedulerException, RemoteException;

    Trigger getTrigger(SchedulingContext ctxt, String triggerName,
            String triggerGroup) throws SchedulerException, RemoteException;

    int getTriggerState(SchedulingContext ctxt, String triggerName,
            String triggerGroup) throws SchedulerException, RemoteException;

    void addCalendar(SchedulingContext ctxt, String calName,
            Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException,
            RemoteException;

    boolean deleteCalendar(SchedulingContext ctxt, String calName)
        throws SchedulerException, RemoteException;

    Calendar getCalendar(SchedulingContext ctxt, String calName)
        throws SchedulerException, RemoteException;

    String[] getCalendarNames(SchedulingContext ctxt)
        throws SchedulerException, RemoteException;

    void addGlobalJobListener(JobListener jobListener)
        throws RemoteException;

    void addJobListener(JobListener jobListener) throws RemoteException;

    boolean removeGlobalJobListener(String name) throws RemoteException;

    boolean removeJobListener(String name) throws RemoteException;

    List getGlobalJobListeners() throws RemoteException;

    Set getJobListenerNames() throws RemoteException;

    JobListener getGlobalJobListener(String name) throws RemoteException;

    JobListener getJobListener(String name) throws RemoteException;

    void addGlobalTriggerListener(TriggerListener triggerListener)
        throws RemoteException;

    void addTriggerListener(TriggerListener triggerListener)
        throws RemoteException;

    boolean removeGlobalTriggerListener(String name)
        throws RemoteException;

    boolean removeTriggerListener(String name) throws RemoteException;

    List getGlobalTriggerListeners() throws RemoteException;

    Set getTriggerListenerNames() throws RemoteException;

    TriggerListener getGlobalTriggerListener(String name)
        throws RemoteException;

    TriggerListener getTriggerListener(String name)
        throws RemoteException;

    void addSchedulerListener(SchedulerListener schedulerListener)
        throws RemoteException;

    boolean removeSchedulerListener(SchedulerListener schedulerListener)
        throws RemoteException;

    List getSchedulerListeners() throws RemoteException;

    boolean interrupt(SchedulingContext ctxt, String jobName, String groupName) throws UnableToInterruptJobException,RemoteException ;
}
