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

package org.terracotta.quartz.collections;

import org.quartz.Calendar;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.terracotta.quartz.wrappers.FiredTrigger;
import org.terracotta.quartz.wrappers.JobWrapper;
import org.terracotta.quartz.wrappers.TriggerWrapper;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.collections.ToolkitMap;
import org.terracotta.toolkit.concurrent.locks.ToolkitLock;
import org.terracotta.toolkit.concurrent.locks.ToolkitLockType;
import org.terracotta.toolkit.config.ToolkitMapConfigFields.Consistency;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * How JOBS mappings will look? <br>
 * JobKey(name, groupname) -> JobWrapper <br>
 * groupName -> List<String> <br>
 * List -> allGroupNames<br>
 */
public class ToolkitDSHolder {
  private static final String                                                     JOBS_MAP_PREFIX                     = "_tc_quartz_jobs";
  private static final String                                                     ALL_JOBS_GROUP_NAMES_SET_PREFIX     = "_tc_quartz_grp_names";
  private static final String                                                     PAUSED_GROUPS_SET_PREFIX            = "_tc_quartz_grp_paused_names";
  private static final String                                                     BLOCKED_JOBS_SET_PREFIX             = "_tc_quartz_blocked_jobs";
  private static final String                                                     JOBS_GROUP_MAP_PREFIX               = "_tc_quartz_grp_jobs_";

  private static final String                                                     TRIGGERS_MAP_PREFIX                 = "_tc_quartz_triggers";
  private static final String                                                     TRIGGERS_GROUP_MAP_PREFIX           = "_tc_quartz_grp_triggers_";
  private static final String                                                     ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX = "_tc_quartz_grp_names_triggers";
  private static final String                                                     PAUSED_TRIGGER_GROUPS_SET_PREFIX    = "_tc_quartz_grp_paused_trogger_names";
  private static final String                                                     TIME_TRIGGER_SORTED_SET_PREFIX      = "_tc_time_trigger_sorted_set";
  private static final String                                                     FIRED_TRIGGER_MAP_PREFIX            = "_tc_quartz_fired_trigger";
  private static final String                                                     CALENDAR_WRAPPER_MAP_PREFIX         = "_tc_quartz_calendar_wrapper";
  private static final String                                                     SINGLE_LOCK_NAME_PREFIX             = "_tc_quartz_single_lock";

  private static final String                                                     DELIMETER                           = "|";
  private final String                                                            jobStoreName;
  protected final Toolkit                                                         toolkit;

  private final AtomicReference<SerializedToolkitMap<JobKey, JobWrapper>>         jobsMapReference                    = new AtomicReference<SerializedToolkitMap<JobKey, JobWrapper>>();
  private final AtomicReference<SerializedToolkitMap<TriggerKey, TriggerWrapper>> triggersMapReference                = new AtomicReference<SerializedToolkitMap<TriggerKey, TriggerWrapper>>();

  private final AtomicReference<ToolkitSet<String>>                               allGroupsReference                  = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                               allTriggersGroupsReference          = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                               pausedGroupsReference               = new AtomicReference<ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<JobKey>>                               blockedJobsReference                = new AtomicReference<ToolkitSet<JobKey>>();
  private final ConcurrentHashMap<String, ToolkitSet<String>>                     jobsGroupSet                        = new ConcurrentHashMap<String, ToolkitSet<String>>();
  private final ConcurrentHashMap<String, ToolkitSet<String>>                     triggersGroupSet                    = new ConcurrentHashMap<String, ToolkitSet<String>>();
  private final AtomicReference<ToolkitSet<String>>                               pausedTriggerGroupsReference        = new AtomicReference<ToolkitSet<String>>();

  private final AtomicReference<ToolkitMap<String, FiredTrigger>>                 firedTriggersMapReference           = new AtomicReference<ToolkitMap<String, FiredTrigger>>();
  private final AtomicReference<ToolkitMap<String, Calendar>>                     calendarWrapperMapReference         = new AtomicReference<ToolkitMap<String, Calendar>>();
  private final AtomicReference<TimeTriggerSet>                                   timeTriggerSetReference             = new AtomicReference<TimeTriggerSet>();

  public ToolkitDSHolder(String jobStoreName, Toolkit toolkit) {
    this.jobStoreName = jobStoreName;
    this.toolkit = toolkit;
  }

  protected final String generateName(String prefix) {
    return prefix + DELIMETER + jobStoreName;
  }

  public ToolkitMap<JobKey, JobWrapper> getOrCreateJobsMap() {
    String jobsMapName = generateName(JOBS_MAP_PREFIX);
    SerializedToolkitMap<JobKey, JobWrapper> temp = new SerializedToolkitMap<JobKey, JobWrapper>(
                                                                                                 toolkitMap(jobsMapName));
    jobsMapReference.compareAndSet(null, temp);
    return jobsMapReference.get();
  }

  protected ToolkitMap toolkitMap(String nameOfMap) {
    return toolkit.getCache(nameOfMap,
                            toolkit.getConfigBuilderFactory().newToolkitCacheConfigBuilder()
                                .consistency(Consistency.STRONG).build());
  }

  public ToolkitMap<TriggerKey, TriggerWrapper> getOrCreateTriggersMap() {
    String triggersMapName = generateName(TRIGGERS_MAP_PREFIX);
    SerializedToolkitMap<TriggerKey, TriggerWrapper> temp = new SerializedToolkitMap<TriggerKey, TriggerWrapper>(
                                                                                                                 toolkitMap(triggersMapName));
    triggersMapReference.compareAndSet(null, temp);
    return triggersMapReference.get();
  }

  public ToolkitMap<String, FiredTrigger> getOrCreateFiredTriggersMap() {
    String firedTriggerMapName = generateName(FIRED_TRIGGER_MAP_PREFIX);
    ToolkitMap<String, FiredTrigger> temp = toolkitMap(firedTriggerMapName);
    firedTriggersMapReference.compareAndSet(null, temp);
    return firedTriggersMapReference.get();
  }

  public ToolkitMap<String, Calendar> getOrCreateCalendarWrapperMap() {
    String calendarWrapperName = generateName(CALENDAR_WRAPPER_MAP_PREFIX);
    ToolkitMap<String, Calendar> temp = toolkitMap(calendarWrapperName);
    calendarWrapperMapReference.compareAndSet(null, temp);
    return calendarWrapperMapReference.get();
  }

  public Set<String> getOrCreateAllGroupsSet() {
    String allGrpSetNames = generateName(ALL_JOBS_GROUP_NAMES_SET_PREFIX);
    ToolkitSet<String> temp = new ToolkitSet<String>(toolkit.getList(allGrpSetNames));
    allGroupsReference.compareAndSet(null, temp);

    return allGroupsReference.get();
  }

  public Set<JobKey> getOrCreateBlockedJobsSet() {
    String blockedJobsSetName = generateName(BLOCKED_JOBS_SET_PREFIX);
    ToolkitSet<JobKey> temp = new ToolkitSet<JobKey>(toolkit.getList(blockedJobsSetName));
    blockedJobsReference.compareAndSet(null, temp);

    return blockedJobsReference.get();
  }

  public Set<String> getOrCreatePausedGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_GROUPS_SET_PREFIX);
    ToolkitSet<String> temp = new ToolkitSet<String>(toolkit.getList(pausedGrpsSetName));
    pausedGroupsReference.compareAndSet(null, temp);

    return pausedGroupsReference.get();
  }

  public Set<String> getOrCreatePausedTriggerGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_TRIGGER_GROUPS_SET_PREFIX);
    ToolkitSet<String> temp = new ToolkitSet<String>(toolkit.getList(pausedGrpsSetName));
    pausedTriggerGroupsReference.compareAndSet(null, temp);

    return pausedTriggerGroupsReference.get();
  }

  public Set<String> getOrCreateJobsGroupMap(String name) {
    synchronized (jobsGroupSet) {
      ToolkitSet<String> set = jobsGroupSet.get(name);

      if (set != null) { return set; }

      String nameForMap = generateName(JOBS_GROUP_MAP_PREFIX + name);
      set = new ToolkitSet<String>(toolkit.getList(nameForMap));
      ToolkitSet<String> oldSet = jobsGroupSet.putIfAbsent(name, set);

      return oldSet != null ? oldSet : set;
    }
  }

  public void removeJobsGroupMap(String name) {
    synchronized (jobsGroupSet) {
      ToolkitSet set = jobsGroupSet.remove(name);
      if (set != null) {
        set.destroy();
      }
    }
  }

  public Set<String> getOrCreateTriggersGroupMap(String name) {
    synchronized (triggersGroupSet) {
      ToolkitSet<String> set = triggersGroupSet.get(name);

      if (set != null) { return set; }

      String nameForMap = generateName(TRIGGERS_GROUP_MAP_PREFIX + name);
      set = new ToolkitSet<String>(toolkit.getList(nameForMap));
      ToolkitSet<String> oldSet = triggersGroupSet.putIfAbsent(name, set);

      return oldSet != null ? oldSet : set;
    }
  }

  public void removeTriggersGroupMap(String name) {
    synchronized (triggersGroupSet) {
      ToolkitSet set = triggersGroupSet.remove(name);
      if (set != null) {
        set.destroy();
      }
    }
  }

  public Set<String> getOrCreateAllTriggersGroupsSet() {
    String allTriggersGrpsName = generateName(ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX);
    ToolkitSet<String> temp = new ToolkitSet<String>(toolkit.getList(allTriggersGrpsName));
    allTriggersGroupsReference.compareAndSet(null, temp);

    return allTriggersGroupsReference.get();
  }

  public TimeTriggerSet getOrCreateTimeTriggerSet() {
    String triggerSetName = generateName(TIME_TRIGGER_SORTED_SET_PREFIX);
    TimeTriggerSet set = new TimeTriggerSet(toolkit.getSortedSet(triggerSetName));
    timeTriggerSetReference.compareAndSet(null, set);

    return timeTriggerSetReference.get();
  }

  public ToolkitLock getLock(ToolkitLockType lockType) {
    String lockName = generateName(SINGLE_LOCK_NAME_PREFIX);
    return toolkit.getLock(lockName, lockType);
  }
}
