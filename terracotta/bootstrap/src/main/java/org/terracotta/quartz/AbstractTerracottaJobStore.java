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
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.client.TerracottaClientStaticFactory;
import org.terracotta.toolkit.client.ToolkitClient;

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
  private volatile ToolkitClient                client;
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
    client = TerracottaClientStaticFactory.getFactory().getOrCreateClient(isURLConfig,
                                                                          isURLConfig ? tcConfigUrl : tcConfig);

    try {
      realJobStore = getRealStore(client.getToolkit());
    } catch (Exception e) {
      throw new SchedulerConfigException("Unable to create Terracotta client", e);
    }
  }

  abstract TerracottaJobStoreExtensions getRealStore(Toolkit toolkit);

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

  public List<OperableTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow)
      throws JobPersistenceException {
    return realJobStore.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
  }

  public List<String> getCalendarNames() throws JobPersistenceException {
    return realJobStore.getCalendarNames();
  }

  public List<String> getJobGroupNames() throws JobPersistenceException {
    return realJobStore.getJobGroupNames();
  }

  public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.getJobKeys(matcher);
  }

  public int getNumberOfCalendars() throws JobPersistenceException {
    return realJobStore.getNumberOfCalendars();
  }

  public int getNumberOfJobs() throws JobPersistenceException {
    return realJobStore.getNumberOfJobs();
  }

  public int getNumberOfTriggers() throws JobPersistenceException {
    return realJobStore.getNumberOfTriggers();
  }

  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    return realJobStore.getPausedTriggerGroups();
  }

  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    return realJobStore.getTriggerGroupNames();
  }

  public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.getTriggerKeys(matcher);
  }

  public List<OperableTrigger> getTriggersForJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.getTriggersForJob(jobKey);
  }

  public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.getTriggerState(triggerKey);
  }

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

  public void pauseAll() throws JobPersistenceException {
    realJobStore.pauseAll();
  }

  public void pauseJob(JobKey jobKey) throws JobPersistenceException {
    realJobStore.pauseJob(jobKey);
  }

  public Collection<String> pauseJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.pauseJobs(matcher);
  }

  public void pauseTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    realJobStore.pauseTrigger(triggerKey);
  }

  public Collection<String> pauseTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.pauseTriggers(matcher);
  }

  public void releaseAcquiredTrigger(OperableTrigger trigger) throws JobPersistenceException {
    realJobStore.releaseAcquiredTrigger(trigger);
  }

  public boolean removeCalendar(String calName) throws JobPersistenceException {
    return realJobStore.removeCalendar(calName);
  }

  public boolean removeJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.removeJob(jobKey);
  }

  public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.removeTrigger(triggerKey);
  }

  public boolean replaceTrigger(TriggerKey triggerKey, OperableTrigger newTrigger) throws JobPersistenceException {
    return realJobStore.replaceTrigger(triggerKey, newTrigger);
  }

  public void resumeAll() throws JobPersistenceException {
    realJobStore.resumeAll();
  }

  public void resumeJob(JobKey jobKey) throws JobPersistenceException {
    realJobStore.resumeJob(jobKey);
  }

  public Collection<String> resumeJobs(GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    return realJobStore.resumeJobs(matcher);
  }

  public void resumeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    realJobStore.resumeTrigger(triggerKey);
  }

  public Collection<String> resumeTriggers(GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    return realJobStore.resumeTriggers(matcher);
  }

  public Calendar retrieveCalendar(String calName) throws JobPersistenceException {
    return realJobStore.retrieveCalendar(calName);
  }

  public JobDetail retrieveJob(JobKey jobKey) throws JobPersistenceException {
    return realJobStore.retrieveJob(jobKey);
  }

  public OperableTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.retrieveTrigger(triggerKey);
  }

  public void schedulerStarted() throws SchedulerException {
    realJobStore.schedulerStarted();
  }

  public void schedulerPaused() {
    realJobStore.schedulerPaused();
  }

  public void schedulerResumed() {
    realJobStore.schedulerResumed();
  }

  public void setInstanceId(String schedInstId) {
    this.schedInstId = schedInstId;
  }

  public void setInstanceName(String schedName) {
    this.schedName = schedName;
  }

  public void setThreadPoolSize(final int poolSize) {
    realJobStore.setThreadPoolSize(poolSize);
  }

  public void shutdown() {
    if (realJobStore != null) {
      realJobStore.shutdown();
    }
    if (client != null) {
      client.shutdown();
    }
  }

  public void storeCalendar(String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
      throws ObjectAlreadyExistsException, JobPersistenceException {
    realJobStore.storeCalendar(name, calendar, replaceExisting, updateTriggers);
  }

  public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeJob(newJob, replaceExisting);
  }

  public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeJobAndTrigger(newJob, newTrigger);
  }

  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws ObjectAlreadyExistsException,
      JobPersistenceException {
    realJobStore.storeTrigger(newTrigger, replaceExisting);
  }

  public boolean supportsPersistence() {
    return true;
  }

  public void triggeredJobComplete(OperableTrigger trigger, JobDetail jobDetail,
                                   Trigger.CompletedExecutionInstruction instruction) throws JobPersistenceException {
    realJobStore.triggeredJobComplete(trigger, jobDetail, instruction);
  }

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

  public long getEstimatedTimeToReleaseAndAcquireTrigger() {
    return realJobStore.getEstimatedTimeToReleaseAndAcquireTrigger();
  }

  public void setEstimatedTimeToReleaseAndAcquireTrigger(long estimate) {
    this.estimatedTimeToReleaseAndAcquireTrigger = estimate;
  }

  public boolean isClustered() {
    return true;
  }

  public boolean checkExists(final JobKey jobKey) throws JobPersistenceException {
    return realJobStore.checkExists(jobKey);
  }

  public boolean checkExists(final TriggerKey triggerKey) throws JobPersistenceException {
    return realJobStore.checkExists(triggerKey);
  }

  public void clearAllSchedulingData() throws JobPersistenceException {
    realJobStore.clearAllSchedulingData();
  }

  public boolean removeTriggers(List<TriggerKey> arg0) throws JobPersistenceException {
    return realJobStore.removeTriggers(arg0);
  }

  public boolean removeJobs(List<JobKey> arg0) throws JobPersistenceException {
    return realJobStore.removeJobs(arg0);
  }

  public void storeJobsAndTriggers(Map<JobDetail, List<Trigger>> arg0, boolean arg1)
      throws ObjectAlreadyExistsException, JobPersistenceException {
    realJobStore.storeJobsAndTriggers(arg0, arg1);
  }

  protected TerracottaJobStoreExtensions getRealJobStore() {
    return realJobStore;
  }
}
