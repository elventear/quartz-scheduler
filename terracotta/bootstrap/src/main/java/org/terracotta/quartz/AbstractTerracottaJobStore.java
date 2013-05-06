/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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
package org.terracotta.quartz;

import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredResult;
import org.terracotta.toolkit.internal.ToolkitInternal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Snaps
 */
public abstract class AbstractTerracottaJobStore implements JobStore {
  public static final String                    TC_CONFIG_PROP                          = StdSchedulerFactory.PROP_JOB_STORE_PREFIX
                                                                                          + ".tcConfig";
  public static final String                    TC_CONFIGURL_PROP                       = StdSchedulerFactory.PROP_JOB_STORE_PREFIX
                                                                                          + ".tcConfigUrl";
  private volatile ToolkitInternal              toolkit;
  private volatile TerracottaJobStoreExtensions realJobStore;
  private String                                tcConfig                                = null;
  private String                                tcConfigUrl                             = null;
  private String                                schedInstId                             = null;
  private String                                schedName                               = null;
  private Long                                  misFireThreshold                        = null;
  private String                                synchWrite                              = null;
  private Long                                  estimatedTimeToReleaseAndAcquireTrigger = null;

  private void init() throws SchedulerConfigException {
    if (realJobStore != null) { return; }

    if ((tcConfig != null) && (tcConfigUrl != null)) {
      //
      throw new SchedulerConfigException("Both " + TC_CONFIG_PROP + " and " + TC_CONFIGURL_PROP
                                         + " are set in your properties. Please define only one of them");
    }

    if ((tcConfig == null) && (tcConfigUrl == null)) {
      //
      throw new SchedulerConfigException("Neither " + TC_CONFIG_PROP + " or " + TC_CONFIGURL_PROP
                                         + " are set in your properties. Please define one of them");
    }

    final boolean isURLConfig = tcConfig == null;
    TerracottaToolkitBuilder toolkitBuilder = new TerracottaToolkitBuilder();
    if (isURLConfig) {
      toolkitBuilder.setTCConfigUrl(tcConfigUrl);
    } else {
      toolkitBuilder.setTCConfigSnippet(tcConfig);
    }
    toolkitBuilder.addTunnelledMBeanDomain("quartz");
    toolkit = (ToolkitInternal) toolkitBuilder.buildToolkit();

    try {
      realJobStore = getRealStore(toolkit);
    } catch (Exception e) {
      throw new SchedulerConfigException("Unable to create Terracotta client", e);
    }
  }

  abstract TerracottaJobStoreExtensions getRealStore(ToolkitInternal toolkitParam);

  public String getUUID() {
    if (realJobStore == null) {
      try {
        init();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return realJobStore.getUUID();
  }

  public void setMisfireThreshold(long threshold) {
    this.misFireThreshold = threshold;
  }

  @Override
  public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow)
      throws JobPersistenceException {
    return realJobStore.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
  }

  @Override
  public List<String> getCalendarNames() throws JobPersistenceException {
    return realJobStore.getCalendarNames();
  }

  @Override
  public List<String> getJobGroupNames() throws JobPersistenceException {
    return realJobStore.getJobGroupNames();
  }

  @Override
  public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.getJobKeys(matcher);
  }

  @Override
  public int getNumberOfCalendars() throws JobPersistenceException {
    return realJobStore.getNumberOfCalendars();
  }

  @Override
  public int getNumberOfJobs() throws JobPersistenceException {
    return realJobStore.getNumberOfJobs();
  }

  @Override
  public int getNumberOfTriggers() throws JobPersistenceException {
    return realJobStore.getNumberOfTriggers();
  }

  @Override
  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    return realJobStore.getPausedTriggerGroups();
  }

  @Override
  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    return realJobStore.getTriggerGroupNames();
  }

  @Override
  public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.getTriggerKeys(matcher);
  }

  @Override
  public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.getTriggersForJob(jobKey);
  }

  @Override
  public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.getTriggerState(triggerKey);
  }

  @Override
  public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
    init();
    realJobStore.setInstanceId(schedInstId);
    realJobStore.setInstanceName(schedName);

    if (misFireThreshold != null) {
      realJobStore.setMisfireThreshold(misFireThreshold);
    }

    if (synchWrite != null) {
      realJobStore.setSynchronousWrite(synchWrite);
    }

    if (estimatedTimeToReleaseAndAcquireTrigger != null) {
      realJobStore.setEstimatedTimeToReleaseAndAcquireTrigger(estimatedTimeToReleaseAndAcquireTrigger);
    }

    realJobStore.initialize(loadHelper, signaler);
  }

  @Override
  public void pauseAll() throws JobPersistenceException {
    realJobStore.pauseAll();
  }

  @Override
  public void pauseJob(JobKey jobKey) throws JobPersistenceException {
    realJobStore.pauseJob(jobKey);
  }

  @Override
  public Collection<String> pauseJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.pauseJobs(matcher);
  }

  @Override
  public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    realJobStore.pauseTrigger(triggerKey);
  }

  @Override
  public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.pauseTriggers(matcher);
  }

  @Override
  public void releaseAcquiredTrigger(OperableTrigger trigger) throws JobPersistenceException {
    realJobStore.releaseAcquiredTrigger(trigger);
  }

  @Override
  public boolean removeCalendar(String calName) throws JobPersistenceException {
    return realJobStore.removeCalendar(calName);
  }

  @Override
  public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.removeJob(jobKey);
  }

  @Override
  public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.removeTrigger(triggerKey);
  }

  @Override
  public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
    return realJobStore.replaceTrigger(triggerKey, newTrigger);
  }

  @Override
  public void resumeAll() throws JobPersistenceException {
    realJobStore.resumeAll();
  }

  @Override
  public void resumeJob(JobKey jobKey) throws JobPersistenceException {
    realJobStore.resumeJob(jobKey);
  }

  @Override
  public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.resumeJobs(matcher);
  }

  @Override
  public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    realJobStore.resumeTrigger(triggerKey);
  }

  @Override
  public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.resumeTriggers(matcher);
  }

  @Override
  public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
    return realJobStore.retrieveCalendar(calName);
  }

  @Override
  public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.retrieveJob(jobKey);
  }

  @Override
  public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.retrieveTrigger(triggerKey);
  }

  @Override
  public void schedulerStarted() throws SchedulerException {
    realJobStore.schedulerStarted();
  }

  @Override
  public void schedulerPaused() {
    realJobStore.schedulerPaused();
  }

  @Override
  public void schedulerResumed() {
    realJobStore.schedulerResumed();
  }

  @Override
  public void setInstanceId(String schedInstId) {
    this.schedInstId = schedInstId;
  }

  @Override
  public void setInstanceName(String schedName) {
    this.schedName = schedName;
  }

  @Override
  public void setThreadPoolSize(final int poolSize) {
    realJobStore.setThreadPoolSize(poolSize);
  }

  @Override
  public void shutdown() {
    if (realJobStore != null) {
      realJobStore.shutdown();
    }
    if (toolkit != null) {
      toolkit.shutdown();
    }
  }

  @Override
  public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
      throws ObjectAlreadyExistsException, JobPersistenceException {
    realJobStore.storeCalendar(name, calendar, replaceExisting, updateTriggers);
  }

  @Override
  public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeJob(newJob, replaceExisting);
  }

  @Override
  public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeJobAndTrigger(newJob, newTrigger);
  }

  @Override
  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeTrigger(newTrigger, replaceExisting);
  }

  @Override
  public boolean supportsPersistence() {
    return true;
  }

  @Override
  public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail,
                                   Trigger.CompletedExecutionInstruction instruction) throws JobPersistenceException {
    realJobStore.triggeredJobComplete(trigger, jobDetail, instruction);
  }

  @Override
  public List<TriggerFiredResult> triggersFired(List<OperableTrigger> triggers) throws JobPersistenceException {
    return realJobStore.triggersFired(triggers);
  }

  public void setTcConfig(String tcConfig) {
    this.tcConfig = tcConfig.trim();
  }

  public void setTcConfigUrl(String tcConfigUrl) {
    this.tcConfigUrl = tcConfigUrl.trim();
  }

  public void setSynchronousWrite(String synchWrite) {
    this.synchWrite = synchWrite;
  }

  @Override
  public long getEstimatedTimeToReleaseAndAcquireTrigger() {
    return realJobStore.getEstimatedTimeToReleaseAndAcquireTrigger();
  }

  public void setEstimatedTimeToReleaseAndAcquireTrigger(long estimate) {
    this.estimatedTimeToReleaseAndAcquireTrigger = estimate;
  }

  @Override
  public boolean isClustered() {
    return true;
  }

  @Override
  public boolean checkExists(final JobKey jobKey) throws JobPersistenceException {
    return realJobStore.checkExists(jobKey);
  }

  @Override
  public boolean checkExists(final TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.checkExists(triggerKey);
  }

  @Override
  public void clearAllSchedulingData() throws JobPersistenceException {
    realJobStore.clearAllSchedulingData();
  }

  @Override
  public boolean removeTriggers(List<TriggerKey> arg0) throws JobPersistenceException {
    return realJobStore.removeTriggers(arg0);
  }

  @Override
  public boolean removeJobs(List<JobKey> arg0) throws JobPersistenceException {
    return realJobStore.removeJobs(arg0);
  }

  @Override
  public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> arg0, boolean arg1)
      throws ObjectAlreadyExistsException, JobPersistenceException {
    realJobStore.storeJobsAndTriggers(arg0, arg1);
  }

  protected TerracottaJobStoreExtensions getRealJobStore() {
    return realJobStore;
  }
}
