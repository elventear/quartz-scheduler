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
  package org.quartz.core;

import java.util.Timer;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.listeners.SchedulerListenerSupport;
import org.quartz.utils.counter.CounterConfig;
import org.quartz.utils.counter.CounterManager;
import org.quartz.utils.counter.CounterManagerImpl;
import org.quartz.utils.counter.sampled.SampledCounter;
import org.quartz.utils.counter.sampled.SampledCounterConfig;
import org.quartz.utils.counter.sampled.SampledRateCounterConfig;

public class SampledStatisticsImpl extends SchedulerListenerSupport implements SampledStatistics, JobListener, SchedulerListener {
  private final QuartzScheduler scheduler;
  
  private static final String NAME = "QuartzSampledStatistics";
  
    private static final int DEFAULT_HISTORY_SIZE = 30;
    private static final int DEFAULT_INTERVAL_SECS = 1;
    private final static SampledCounterConfig DEFAULT_SAMPLED_COUNTER_CONFIG = new SampledCounterConfig(DEFAULT_INTERVAL_SECS,
            DEFAULT_HISTORY_SIZE, true, 0L);
    private final static SampledRateCounterConfig DEFAULT_SAMPLED_RATE_COUNTER_CONFIG = new SampledRateCounterConfig(DEFAULT_INTERVAL_SECS,
            DEFAULT_HISTORY_SIZE, true);

    private volatile CounterManager counterManager;
    private final SampledCounter jobsScheduledCount;
    private final SampledCounter jobsExecutingCount;
    private final SampledCounter jobsCompletedCount;
  
  SampledStatisticsImpl(QuartzScheduler scheduler) {
    this.scheduler = scheduler;
    
        counterManager = new CounterManagerImpl(new Timer(NAME+"Timer"));
        jobsScheduledCount = createSampledCounter(DEFAULT_SAMPLED_COUNTER_CONFIG);
        jobsExecutingCount = createSampledCounter(DEFAULT_SAMPLED_COUNTER_CONFIG);
        jobsCompletedCount = createSampledCounter(DEFAULT_SAMPLED_COUNTER_CONFIG);
        
        scheduler.addInternalSchedulerListener(this);
        scheduler.addInternalJobListener(this);
  }
  
  public void shutdown() {
      counterManager.shutdown(true);
  }
  
    private SampledCounter createSampledCounter(CounterConfig defaultCounterConfig) {
        return (SampledCounter) counterManager.createCounter(defaultCounterConfig);
    }
  
    /**
     * Clears the collected statistics. Resets all counters to zero
     */
    public void clearStatistics() {
      jobsScheduledCount.getAndReset();
      jobsExecutingCount.getAndReset();
      jobsCompletedCount.getAndReset();
    }
    
  public long getJobsCompletedMostRecentSample() {
        return jobsCompletedCount.getMostRecentSample().getCounterValue();
  }

  public long getJobsExecutingMostRecentSample() {
        return jobsExecutingCount.getMostRecentSample().getCounterValue();
  }

  public long getJobsScheduledMostRecentSample() {
        return jobsScheduledCount.getMostRecentSample().getCounterValue();
  }

  public String getName() {
    return NAME;
  }

    @Override
    public void jobScheduled(Trigger trigger) {
      jobsScheduledCount.increment();
    }
  
  public void jobExecutionVetoed(JobExecutionContext context) {
    /**/
  }

  public void jobToBeExecuted(JobExecutionContext context) {
    jobsExecutingCount.increment();
  }

  public void jobWasExecuted(JobExecutionContext context,
      JobExecutionException jobException) {
    jobsCompletedCount.increment();
  }

  @Override
    public void jobAdded(JobDetail jobDetail) {
    /**/
  }

  public void jobDeleted(String jobName, String groupName) {
    /**/
  }
}
