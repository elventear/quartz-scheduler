
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

package org.quartz.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
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

    SchedulerContext getSchedulerContext() throws SchedulerException, RemoteException;

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

    boolean isClustered() throws RemoteException;

    Class getThreadPoolClass() throws RemoteException;

    int getThreadPoolSize() throws RemoteException;

    List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException, RemoteException;

    Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException, RemoteException;

    Date scheduleJob(Trigger trigger) throws SchedulerException, RemoteException;

    void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException, RemoteException;

    boolean deleteJob(String jobName, String groupName) throws SchedulerException, RemoteException;

    boolean unscheduleJob(String triggerName, String groupName) throws SchedulerException, RemoteException;

    Date rescheduleJob(String triggerName, String groupName, Trigger newTrigger) throws SchedulerException, RemoteException;
        
    
    void triggerJob(String jobName, String groupName, JobDataMap data) throws SchedulerException, RemoteException;

    void triggerJobWithVolatileTrigger( String jobName, String groupName, JobDataMap data) throws SchedulerException, RemoteException;

    void pauseTrigger(String triggerName, String groupName) throws SchedulerException, RemoteException;

    void pauseTriggerGroup(String groupName) throws SchedulerException, RemoteException;

    void pauseJob(String jobName, String groupName) throws SchedulerException, RemoteException;

    void pauseJobGroup(String groupName) throws SchedulerException, RemoteException;

    void resumeTrigger(String triggerName, String groupName) throws SchedulerException, RemoteException;

    void resumeTriggerGroup(String groupName) throws SchedulerException, RemoteException;

    Set<String> getPausedTriggerGroups() throws SchedulerException, RemoteException;
    
    void resumeJob(String jobName, String groupName) throws SchedulerException, RemoteException;

    void resumeJobGroup(String groupName) throws SchedulerException, RemoteException;

    void pauseAll() throws SchedulerException, RemoteException;

    void resumeAll() throws SchedulerException, RemoteException;

    List<String> getJobGroupNames() throws SchedulerException, RemoteException;

    List<String> getJobNames(String groupName) throws SchedulerException, RemoteException;

    List<? extends Trigger> getTriggersOfJob(String jobName, String groupName) throws SchedulerException, RemoteException;

    List<String> getTriggerGroupNames() throws SchedulerException, RemoteException;

    List<String> getTriggerNames(String groupName) throws SchedulerException, RemoteException;

    JobDetail getJobDetail(String jobName, String jobGroup) throws SchedulerException, RemoteException;

    Trigger getTrigger(String triggerName, String triggerGroup) throws SchedulerException, RemoteException;

    int getTriggerState(String triggerName, String triggerGroup) throws SchedulerException, RemoteException;

    void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException, RemoteException;

    boolean deleteCalendar(String calName) throws SchedulerException, RemoteException;

    Calendar getCalendar(String calName) throws SchedulerException, RemoteException;

    List<String> getCalendarNames() throws SchedulerException, RemoteException;

    void addGlobalJobListener(JobListener jobListener) throws RemoteException;

    boolean removeGlobalJobListener(String name) throws RemoteException;

    List<JobListener> getGlobalJobListeners() throws RemoteException;

    JobListener getGlobalJobListener(String name) throws RemoteException;

    void addGlobalTriggerListener(TriggerListener triggerListener) throws RemoteException;

    boolean removeGlobalTriggerListener(String name) throws RemoteException;

    List<TriggerListener> getGlobalTriggerListeners() throws RemoteException;

    TriggerListener getGlobalTriggerListener(String name) throws RemoteException;

    void addSchedulerListener(SchedulerListener schedulerListener) throws RemoteException;

    boolean removeSchedulerListener(SchedulerListener schedulerListener) throws RemoteException;

    List<SchedulerListener> getSchedulerListeners() throws RemoteException;

    boolean interrupt(String jobName, String groupName) throws UnableToInterruptJobException,RemoteException ;
}
