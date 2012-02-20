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

import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.terracotta.collections.quartz.DistributedSortedSet;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

public class TimeTriggerSet {

  private final DistributedSortedSet<TimeTrigger> timeTriggers = new DistributedSortedSet<TimeTrigger>();

  private transient HashSet<Object>               hardRefs;

  boolean add(TriggerWrapper tw) {
    return timeTriggers.add(new TimeTrigger(tw));
  }

  boolean remove(TriggerWrapper tw) {
    return timeTriggers.remove(new TimeTrigger(tw));
  }

  TriggerWrapper removeFirst() {
    TimeTrigger tt = timeTriggers.removeFirst();
    return tt == null ? null : tt.getTriggerWrapper();
  }

  void initialize() {
    hardRef(timeTriggers);
    timeTriggers.initialize((Comparator) new TimeTriggerComparator());
  }

  private synchronized void hardRef(Object o) {
    if (hardRefs == null) {
      hardRefs = new HashSet<Object>();
    }
    hardRefs.add(o);
  }

  public int size() {
    return timeTriggers.size();
  }

  private static class TimeTriggerComparator implements Comparator<TimeTrigger> {

    public int compare(TimeTrigger tt1, TimeTrigger tt2) {
      return Trigger.TriggerTimeComparator.compare(tt1.getNextFireTime(), tt1.getPriority(), tt1.getTriggerKey(),
                                                   tt2.getNextFireTime(), tt2.getPriority(), tt2.getTriggerKey());
    }
  }

  private static class TimeTrigger implements DistributedSortedSet.Element<ClusteredTriggerKey> {

    private final TriggerWrapper tw;
    private final Long           nextFireTime;
    private final int            priority;

    TimeTrigger(TriggerWrapper tw) {
      this.tw = tw;
      Date next = tw.getTrigger().getNextFireTime();
      this.nextFireTime = next == null ? null : next.getTime();
      this.priority = tw.getTrigger().getPriority();
    }

    TriggerKey getTriggerKey() {
      return new TriggerKey(getKey().getName(), getKey().getGroupName());
    }

    int getPriority() {
      return priority;
    }

    Date getNextFireTime() {
      return nextFireTime == null ? null : new Date(nextFireTime);
    }

    TriggerWrapper getTriggerWrapper() {
      return tw;
    }

    public ClusteredTriggerKey getKey() {
      return tw.getKey();
    }

    public void clearLocal() {
      tw.clearLocal();
    }

    public void serialize() {
      tw.serialize();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TimeTrigger) {
        TimeTrigger other = (TimeTrigger) obj;
        return tw.equals(other.tw);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return tw.hashCode();
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + getKey() + ")";
    }

  }

}
